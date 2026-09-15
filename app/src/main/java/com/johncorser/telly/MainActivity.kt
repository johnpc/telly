package com.johncorser.telly

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
import com.johncorser.telly.core.playlistRefresher
import com.johncorser.telly.core.searchDeps
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.core.vodDeps
import com.johncorser.telly.features.onboarding.StartRoute
import com.johncorser.telly.features.pip.PipActivityBridge
import com.johncorser.telly.features.pip.PipState
import com.johncorser.telly.features.playback.PlaybackTime
import com.johncorser.telly.features.playlist.M3uFetcher
import com.johncorser.telly.features.reminders.remindersHub
import kotlinx.coroutines.launch

/** Single-activity entry point; all UI is Compose for TV. */
class MainActivity : ComponentActivity() {
    private val navigator = Navigator(start = Route.Boot)
    private val fetcher by lazy {
        M3uFetcher(userAgentFor = ServiceLocator.playlistFetchUserAgentFor(this))
    }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = ServiceLocator.playlistRepository(this)
        val hooks = playbackHooks(afr)
        restoreStartRoute()
        keepEpgFresh()
        keepPlaylistsFresh()
        setContent {
            RootScreen(
                navigator = navigator,
                repository = repository,
                fetchPlaylist = fetcher::fetch,
                playbackDeps = ServiceLocator.playbackDeps(this, hooks),
                guideDeps = ServiceLocator.guideDeps(this, hooks),
                settingsGraph = settingsGraph(fetcher),
                searchDeps = ServiceLocator.searchDeps(this),
                multiviewDeps = ServiceLocator.multiviewDeps(this),
                vodDeps = ServiceLocator.vodDeps(this),
                onEnterPip = pip::enter,
                reminders = ServiceLocator.remindersHub(this),
            )
        }
    }

    /** Backgrounding is an AFR restore point (the restore-on-stop variant). */
    override fun onStop() {
        afr.onPlaybackStopped()
        super.onStop()
    }

    /** Boot stays blank until Room answers; then playback or onboarding. */
    private fun restoreStartRoute() {
        lifecycleScope.launch {
            val channelCount = ServiceLocator.database(this@MainActivity).channelDao().totalCount()
            navigator.replaceAll(StartRoute.forChannelCount(channelCount))
        }
    }

    /** Update playlists at launch (forced/due) and per minute while running. */
    private fun keepPlaylistsFresh() {
        val refresher = ServiceLocator.playlistRefresher(this, fetcher::fetch)
        lifecycleScope.launch { refresher.run(PlaybackTime.minuteBoundaryTicks(ServiceLocator.clock)) }
    }

    /** Refresh due EPG sources on start and whenever the playlists change. */
    private fun keepEpgFresh() {
        val refresher = ServiceLocator.epgRefresher(this)
        val settings = ServiceLocator.settingsRepository(this)
        lifecycleScope.launch {
            if (settings.get(TellySettings.EPG_UPDATE_ON_APP_START)) refresher.refreshAllNow()
            ServiceLocator.database(this@MainActivity).playlistDao().observeAll().collect {
                refresher.refreshDue()
            }
        }
    }
}
