package com.johncorser.telly

import androidx.lifecycle.lifecycleScope
import com.johncorser.telly.core.ServiceLocator
import com.johncorser.telly.core.playlistRefresher
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.playback.PlaybackTime
import com.johncorser.telly.features.playlist.M3uFetcher
import kotlinx.coroutines.launch

// MainActivity's background-freshness loops (kept out of MainActivity.kt
// for the file-length gate; the policies live in the tested refreshers).

/** Update playlists at launch (forced/due) and per minute while running. */
internal fun MainActivity.keepPlaylistsFresh(fetcher: M3uFetcher) {
    val refresher = ServiceLocator.playlistRefresher(this, fetcher::fetch)
    lifecycleScope.launch { refresher.run(PlaybackTime.minuteBoundaryTicks(ServiceLocator.clock)) }
}

/** Refresh due EPG sources on start and whenever the playlists change. */
internal fun MainActivity.keepEpgFresh() {
    val refresher = ServiceLocator.epgRefresher(this)
    val settings = ServiceLocator.settingsRepository(this)
    lifecycleScope.launch {
        if (settings.get(TellySettings.EPG_UPDATE_ON_APP_START)) refresher.refreshAllNow()
        ServiceLocator.database(this@keepEpgFresh).playlistDao().observeAll().collect {
            refresher.refreshDue()
        }
    }
}
