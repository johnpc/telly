package com.johncorser.telly.features.epg

import com.johncorser.telly.core.state.StateMachine
import com.johncorser.telly.features.playlist.db.PlaylistEntity

/**
 * The freshness of one playlist's EPG, read off its two persisted stamps —
 * the ONE place the `0 = never` sentinels are interpreted. The refresher's
 * due loop asks [isDue]; "Update EPG" ignores it and refreshes regardless.
 */
@StateMachine("EPG freshness")
sealed interface EpgFreshness {
    /** No successful fetch yet and no failed attempt recorded: always due. */
    data object NeverFetched : EpgFreshness

    /** The last attempt fetched something at [atMs]; due again after the interval. */
    data class Fresh(
        val atMs: Long,
    ) : EpgFreshness

    /**
     * The last attempt at [failedAtMs] fetched nothing ([lastSuccessMs] = 0
     * when it never succeeded). Due again only after the failure backoff —
     * and, when it had succeeded before, after the interval too.
     */
    data class Failed(
        val failedAtMs: Long,
        val lastSuccessMs: Long,
    ) : EpgFreshness

    companion object {
        /** A playlist whose every source failed waits this long before the due loop retries it. */
        const val RETRY_BACKOFF_MS: Long = 5L * 60L * 1000L

        fun of(playlist: PlaylistEntity): EpgFreshness =
            when {
                playlist.epgFailedAtMs > playlist.epgLastUpdatedMs ->
                    Failed(playlist.epgFailedAtMs, playlist.epgLastUpdatedMs)
                playlist.epgLastUpdatedMs > 0 -> Fresh(playlist.epgLastUpdatedMs)
                else -> NeverFetched
            }

        /** The scheduled-refresh decision for [freshness] at [nowMs] under [scheduler]'s interval. */
        fun isDue(
            freshness: EpgFreshness,
            nowMs: Long,
            scheduler: RefreshScheduler,
        ): Boolean =
            when (freshness) {
                NeverFetched -> true
                is Fresh -> scheduler.isDue(freshness.atMs, nowMs)
                is Failed ->
                    nowMs - freshness.failedAtMs >= RETRY_BACKOFF_MS &&
                        scheduler.isDue(freshness.lastSuccessMs, nowMs)
            }
    }
}
