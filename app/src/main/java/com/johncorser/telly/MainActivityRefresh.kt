package com.johncorser.telly

import androidx.lifecycle.lifecycleScope
import com.johncorser.telly.core.ServiceLocator
import com.johncorser.telly.core.epgRefresher
import com.johncorser.telly.core.playlistRefresher
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.playback.PlaybackTime
import com.johncorser.telly.features.playlist.M3uFetcher
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch

// MainActivity's background refresh loops (kept out of MainActivity.kt for
// the file-length gate; the policy lives in the tested refresher/scheduler
// classes — these are thin launch wrappers).

/** Update playlists at launch (forced/due) and per minute while running. */
internal fun MainActivity.keepPlaylistsFresh(fetcher: M3uFetcher) {
    val refresher = ServiceLocator.playlistRefresher(this, fetcher::fetch)
    lifecycleScope.launch { refresher.run(PlaybackTime.minuteBoundaryTicks(ServiceLocator.clock)) }
}

/**
 * Refresh due EPG on start, playlist changes and per-minute ticks. When
 * the playlists CHANGE (any emission after the first), "Update EPG on
 * playlists change" decides between a forced full refresh (ON) and the
 * due-only policy (OFF, the captured default — never-fetched sources such
 * as a freshly added playlist's EPG are always due).
 */
internal fun MainActivity.keepEpgFresh() {
    val refresher = ServiceLocator.epgRefresher(this)
    val settings = ServiceLocator.settingsRepository(this)
    lifecycleScope.launch {
        if (settings.get(TellySettings.EPG_UPDATE_ON_APP_START)) refresher.refreshAllNow()
        ServiceLocator.database(this@keepEpgFresh).playlistDao().observeAll().collectIndexed { index, _ ->
            if (index == 0) {
                refresher.refreshDue()
            } else {
                refresher.onPlaylistsChanged(settings.get(TellySettings.EPG_UPDATE_ON_PLAYLISTS_CHANGE))
            }
        }
    }
    // A failed source is never stamped (epgLastUpdatedMs stays 0), so the
    // scheduler keeps it due and the next minute tick retries transient
    // failures instead of leaving the guide empty until the next launch.
    lifecycleScope.launch {
        PlaybackTime.minuteBoundaryTicks(ServiceLocator.clock).collect { refresher.refreshDue() }
    }
}
