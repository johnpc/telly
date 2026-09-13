package com.johncorser.telly.features.epg

/**
 * Pure refresh policy for playlists and EPG sources. TiviMate's default
 * update interval is 24 hours (ux-spec §2.13/§5: "default/customary
 * interval 24 h"); a lastUpdated of 0 means "never refreshed" — always due.
 */
class RefreshScheduler(
    private val intervalMs: Long = DEFAULT_INTERVAL_MS,
) {
    init {
        require(intervalMs > 0) { "intervalMs must be positive, was $intervalMs" }
    }

    /** True when data stamped [lastUpdatedMs] needs a refresh at [nowMs]. */
    fun isDue(
        lastUpdatedMs: Long,
        nowMs: Long,
    ): Boolean = lastUpdatedMs <= 0 || nowMs - lastUpdatedMs >= intervalMs

    companion object {
        /** TiviMate default EPG/playlist update interval: 24 hours. */
        const val DEFAULT_INTERVAL_MS: Long = 24L * 60L * 60L * 1000L
    }
}
