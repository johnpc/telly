package com.johncorser.telly.features.epg

import com.johncorser.telly.features.playlist.db.PlaylistDao
import kotlinx.coroutines.CancellationException

/**
 * Applies the [RefreshScheduler] policy: refreshes every configured EPG
 * source — the auto-detected `url-tvg` plus the playlist's custom sources —
 * of every playlist whose data is due, and stamps the playlists where at
 * least one source succeeded. Failures are swallowed per source (but logged
 * through [EpgFetch.warn]) so one bad EPG source cannot starve the others;
 * a failed playlist is never stamped, so it stays due and retries on the
 * next scheduled run. Merge rule (the reference free tier
 * cannot show one): sources are fetched auto-detected FIRST, then custom in
 * added order, and [EpgRepository.refresh] replaces a channel's whole
 * schedule per fetched document — so the last source covering a channel
 * owns it, i.e. custom sources take precedence per channel. After a run it
 * trims stored programmes past the "Past days to keep EPG" horizon.
 */
class EpgRefresher(
    private val playlistDao: PlaylistDao,
    private val scheduler: RefreshScheduler,
    private val clock: () -> Long,
    private val fetch: EpgFetch,
    private val retention: EpgRetention = EpgRetention(),
    private val customSources: suspend (playlistUrl: String) -> List<String> = { emptyList() },
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
                    val sources = listOfNotNull(playlist.epgUrl) + customSources(playlist.url)
                    if (sources.isEmpty() || !due(playlist.epgLastUpdatedMs)) return@mapNotNull null
                    if (sources.count { refreshSource(it) } == 0) return@mapNotNull null
                    playlistDao.markEpgUpdated(playlist.id, clock())
                    playlist.id
                }
        retention.trim(clock() - retention.keepPastMs())
        return updated
    }

    private suspend fun refreshSource(url: String): Boolean =
        runCatching { fetch.refresh(url) }
            .onFailure {
                if (it is CancellationException) throw it
                fetch.warn("EPG refresh failed for $url", it)
            }
            .isSuccess

    companion object {
        private const val DAY_MS: Long = 24L * 60L * 60L * 1000L

        /** Captured default: "Past days to keep EPG" = 7. */
        const val DEFAULT_KEEP_PAST_MS: Long = 7L * DAY_MS

        /** Maps the settings value (days) to a keep horizon in ms. */
        fun daysToMs(days: Int): Long = days.coerceAtLeast(0) * DAY_MS
    }
}

/** The per-source fetch seam plus its failure diagnostics. */
class EpgFetch(
    val refresh: suspend (epgUrl: String) -> Int,
    /** Failure log seam; ServiceLocator wires android.util.Log. */
    val warn: (message: String, cause: Throwable) -> Unit = { _, _ -> },
)

/** The "Past days to keep EPG" horizon and the trim that enforces it. */
class EpgRetention(
    val keepPastMs: () -> Long = { EpgRefresher.DEFAULT_KEEP_PAST_MS },
    val trim: suspend (cutoffMs: Long) -> Unit = {},
)
