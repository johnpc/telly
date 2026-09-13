package com.johncorser.telly.features.epg

/**
 * Pure refresh policy for playlists and EPG sources. The interval comes
 * from a provider so Settings -> EPG -> "Update interval, hours" changes
 * apply live. Interval [NEVER_MS] (the captured default "None") means
 * "only fetch data that has never been fetched" — a lastUpdated of 0.
 */
class RefreshScheduler(
    private val intervalMs: () -> Long,
) {
    /** Fixed-interval policy; non-positive fixed intervals are rejected. */
    constructor(intervalMs: Long = DEFAULT_INTERVAL_MS) : this({ intervalMs }) {
        require(intervalMs > 0) { "intervalMs must be positive, was $intervalMs" }
    }

    /** True when data stamped [lastUpdatedMs] needs a refresh at [nowMs]. */
    fun isDue(
        lastUpdatedMs: Long,
        nowMs: Long,
    ): Boolean {
        if (lastUpdatedMs <= 0) return true
        val interval = intervalMs()
        return interval > 0 && nowMs - lastUpdatedMs >= interval
    }

    companion object {
        /** TiviMate default EPG/playlist update interval: 24 hours. */
        const val DEFAULT_INTERVAL_MS: Long = 24L * 60L * 60L * 1000L

        /** The captured default "None": no periodic refresh. */
        const val NEVER_MS: Long = 0L

        const val HOUR_MS: Long = 60L * 60L * 1000L

        /** Maps the settings value (hours, 0 = None) to milliseconds. */
        fun hoursToMs(hours: Int): Long = if (hours <= 0) NEVER_MS else hours * HOUR_MS
    }
}
