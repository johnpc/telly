package com.johncorser.telly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.johncorser.telly.core.ServiceLocator
import com.johncorser.telly.core.navigation.Navigator
import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.onboarding.StartRoute
import com.johncorser.telly.features.playlist.M3uFetcher
import com.johncorser.telly.features.settings.PlaylistUpdater
import com.johncorser.telly.features.settings.SettingsActions
import com.johncorser.telly.features.settings.SettingsBackupManager
import com.johncorser.telly.features.settings.SettingsGraph
import kotlinx.coroutines.launch

/** Single-activity entry point; all UI is Compose for TV. */
class MainActivity : ComponentActivity() {
    private val navigator = Navigator()
    private val fetcher = M3uFetcher()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = ServiceLocator.playlistRepository(this)
        restoreStartRoute()
        keepEpgFresh()
        setContent {
            RootScreen(
                navigator = navigator,
                repository = repository,
                fetchPlaylist = fetcher::fetch,
                settingsGraph = settingsGraph(),
            )
        }
    }

    /** Assembles the settings slice over the shared composition root. */
    private fun settingsGraph(): SettingsGraph {
        val settings = ServiceLocator.settingsRepository(this)
        val repository = ServiceLocator.playlistRepository(this)
        return SettingsGraph(
            settings = settings,
            playlists = repository,
            parental = ParentalControls(settings),
            actions =
                SettingsActions(
                    updater = PlaylistUpdater(fetcher::fetch, repository),
                    updateEpgNow = { ServiceLocator.epgRefresher(this).refreshAllNow() },
                    backup = SettingsBackupManager(settings, repository, filesDir),
                ),
            versionName = appVersionName(),
        )
    }

    private fun appVersionName(): String =
        runCatching { packageManager.getPackageInfo(packageName, 0).versionName }
            .getOrNull() ?: "unknown"

    /** Channels persist in Room: skip onboarding when some already exist. */
    private fun restoreStartRoute() {
        lifecycleScope.launch {
            val channelDao = ServiceLocator.database(this@MainActivity).channelDao()
            StartRoute
                .forCounts(channelDao.totalCount(), channelDao.totalGroupCount())
                ?.let(navigator::replaceAll)
        }
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
