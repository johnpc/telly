package com.johncorser.telly.features.epg

import com.johncorser.telly.features.playlist.db.PlaylistDao
import kotlinx.coroutines.CancellationException

/**
 * Applies the [RefreshScheduler] policy: refreshes the EPG of every playlist
 * whose data is due and stamps the ones that succeed. Failures are swallowed
 * per playlist so one bad EPG source cannot starve the others.
 */
class EpgRefresher(
    private val playlistDao: PlaylistDao,
    private val scheduler: RefreshScheduler,
    private val clock: () -> Long,
    private val refresh: suspend (epgUrl: String) -> Int,
) {
    /** Refreshes everything due; returns the ids of the playlists updated. */
    suspend fun refreshDue(): List<Long> =
        playlistDao
            .all()
            .mapNotNull { playlist ->
                val epgUrl = playlist.epgUrl ?: return@mapNotNull null
                if (!scheduler.isDue(playlist.epgLastUpdatedMs, clock())) return@mapNotNull null
                runCatching { refresh(epgUrl) }
                    .onFailure { if (it is CancellationException) throw it }
                    .map {
                        playlistDao.markEpgUpdated(playlist.id, clock())
                        playlist.id
                    }.getOrNull()
            }
}
