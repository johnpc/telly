package com.johncorser.telly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.johncorser.telly.core.ServiceLocator
import com.johncorser.telly.core.navigation.Navigator
import com.johncorser.telly.features.onboarding.StartRoute
import com.johncorser.telly.features.playlist.M3uFetcher
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
            )
        }
    }

    /** Channels persist in Room: skip onboarding when some already exist. */
    private fun restoreStartRoute() {
        lifecycleScope.launch {
            val channelCount = ServiceLocator.database(this@MainActivity).channelDao().totalCount()
            StartRoute.forChannelCount(channelCount)?.let(navigator::replaceAll)
        }
    }

    /** Refresh due EPG sources on start and whenever the playlists change. */
    private fun keepEpgFresh() {
        val refresher = ServiceLocator.epgRefresher(this)
        lifecycleScope.launch {
            ServiceLocator.database(this@MainActivity).playlistDao().observeAll().collect {
                refresher.refreshDue()
            }
        }
    }
}
