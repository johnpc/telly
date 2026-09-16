package com.johncorser.telly

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.johncorser.telly.core.ServiceLocator
import com.johncorser.telly.core.guideDeps
import com.johncorser.telly.core.multiviewDeps
import com.johncorser.telly.core.navigation.Navigator
import com.johncorser.telly.core.navigation.Route
import com.johncorser.telly.core.playbackDeps
import com.johncorser.telly.core.playlistFetchUserAgentFor
import com.johncorser.telly.core.searchDeps
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.core.settings.withAppLocale
import com.johncorser.telly.core.vodDeps
import com.johncorser.telly.features.onboarding.StartRoute
import com.johncorser.telly.features.pip.PipActivityBridge
import com.johncorser.telly.features.pip.PipState
import com.johncorser.telly.features.playlist.M3uFetcher
import com.johncorser.telly.features.recording.recordingDeps
import com.johncorser.telly.features.reminders.remindersHub
import kotlinx.coroutines.launch

/** Single-activity entry point; all UI is Compose for TV. */
class MainActivity : ComponentActivity() {
    private val navigator = Navigator(start = Route.Boot)
    private val fetcher by lazy {
        M3uFetcher(userAgentFor = ServiceLocator.playlistFetchUserAgentFor(this))
    }
    private val startRoute =
        StartRoute(
            lastChannelOnStart = {
                ServiceLocator.settingsRepository(this).get(TellySettings.LAST_CHANNEL_ON_START)
            },
        )
    private val pip = PipActivityBridge(this, PipState.shared, ::pipOnHome, ::playbackIsFullscreen)
    private val afr by lazy { afrController() }

    private fun pipOnHome() = ServiceLocator.settingsRepository(this).get(TellySettings.PIP_ON_HOME)

    private fun playbackIsFullscreen() = navigator.stack.value.lastOrNull() == Route.Playback

    /** HOME with fullscreen playback up switches to PIP when the setting is on. */
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        pip.onUserLeaveHint()
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration,
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        pip.onModeChanged(isInPictureInPictureMode)
    }

    /**
     * Appearance -> Language: the activity's base context carries the
     * picked locale (thin glue; the mapping lives in AppLanguage and the
     * wrap in AppLocaleContext). Live changes re-resolve in composition
     * via ProvideAppLocale — no appcompat per-app-locale API is available.
     */
    override fun attachBaseContext(newBase: Context) {
        val language = ServiceLocator.settingsRepository(newBase).get(TellySettings.LANGUAGE)
        super.attachBaseContext(newBase.withAppLocale(language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = ServiceLocator.playlistRepository(this)
        val hooks = playbackHooks(afr)
        restoreStartRoute()
        keepEpgFresh()
        keepPlaylistsFresh(fetcher)
        setContent {
            RootScreen(
                navigator = navigator,
                repository = repository,
                fetchPlaylist = fetcher::fetch,
                playbackDeps = ServiceLocator.playbackDeps(this, hooks),
                guideDeps = ServiceLocator.guideDeps(this, hooks) { !startRoute.consumeUntunedStart() },
                settingsGraph = settingsGraph(fetcher),
                searchDeps = ServiceLocator.searchDeps(this),
                multiviewDeps = ServiceLocator.multiviewDeps(this),
                vodDeps = ServiceLocator.vodDeps(this),
                onEnterPip = pip::enter,
                reminders = ServiceLocator.remindersHub(this),
                recordingDeps = ServiceLocator.recordingDeps(this),
            )
        }
    }

    /** Backgrounding is an AFR restore point (the restore-on-stop variant). */
    override fun onStop() {
        afr.onPlaybackStopped()
        super.onStop()
    }

    /** Boot stays blank until Room answers; then playback/guide/onboarding. */
    private fun restoreStartRoute() {
        lifecycleScope.launch {
            val channelCount = ServiceLocator.database(this@MainActivity).channelDao().totalCount()
            navigator.replaceAll(startRoute.forChannelCount(channelCount))
        }
    }
}
