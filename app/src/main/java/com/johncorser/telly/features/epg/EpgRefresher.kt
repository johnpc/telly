package com.johncorser.telly.features.epg

import com.johncorser.telly.features.playlist.db.PlaylistDao
import kotlinx.coroutines.CancellationException

/**
 * Applies the [RefreshScheduler] policy: refreshes the EPG of every playlist
 * whose data is due and stamps the ones that succeed. Failures are swallowed
 * per playlist so one bad EPG source cannot starve the others. After a run
 * it trims stored programmes past the "Past days to keep EPG" horizon.
 */
class EpgRefresher(
    private val playlistDao: PlaylistDao,
    private val scheduler: RefreshScheduler,
    private val clock: () -> Long,
    private val refresh: suspend (epgUrl: String) -> Int,
    private val keepPastMs: () -> Long = { DEFAULT_KEEP_PAST_MS },
    private val trim: suspend (cutoffMs: Long) -> Unit = {},
) {
    /** Refreshes everything due; returns the ids of the playlists updated. */
    suspend fun refreshDue(): List<Long> = refreshWhere { scheduler.isDue(it, clock()) }

    /** Settings -> "Update EPG": refreshes every source regardless of age. */
    suspend fun refreshAllNow(): List<Long> = refreshWhere { true }

    private suspend fun refreshWhere(due: (lastUpdatedMs: Long) -> Boolean): List<Long> {
        val updated =
            playlistDao
                .all()
                .mapNotNull { playlist ->
                    val epgUrl = playlist.epgUrl ?: return@mapNotNull null
                    if (!due(playlist.epgLastUpdatedMs)) return@mapNotNull null
                    runCatching { refresh(epgUrl) }
                        .onFailure { if (it is CancellationException) throw it }
                        .map {
                            playlistDao.markEpgUpdated(playlist.id, clock())
                            playlist.id
                        }.getOrNull()
                }
        trim(clock() - keepPastMs())
        return updated
    }

    companion object {
        private const val DAY_MS: Long = 24L * 60L * 60L * 1000L

        /** Captured default: "Past days to keep EPG" = 7. */
        const val DEFAULT_KEEP_PAST_MS: Long = 7L * DAY_MS

        /** Maps the settings value (days) to a keep horizon in ms. */
        fun daysToMs(days: Int): Long = days.coerceAtLeast(0) * DAY_MS
    }
}
