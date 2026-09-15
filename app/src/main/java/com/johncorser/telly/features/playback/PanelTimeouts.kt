package com.johncorser.telly.features.playback

/**
 * Appearance -> Player -> "Panels timeout, sec", resolved to the three
 * overlay auto-hide durations. The default 5 s maps to EXACTLY today's
 * measured constants (5.35 s info / 5.5 s zap / 5 s quick-bar); other
 * choices keep the same measured offsets over the chosen base.
 */
data class PanelTimeouts(
    val infoMs: Long,
    val zapMs: Long,
    val quickBarMs: Long,
) {
    companion object {
        private const val MS_PER_SECOND = 1_000L
        private const val INFO_EXTRA_MS = 350L
        private const val ZAP_EXTRA_MS = 500L

        val DEFAULT =
            PanelTimeouts(
                infoMs = PlaybackViewModel.INFO_OVERLAY_TIMEOUT_MS,
                zapMs = PlaybackViewModel.ZAP_OVERLAY_TIMEOUT_MS,
                quickBarMs = PlaybackViewModel.QUICK_BAR_TIMEOUT_MS,
            )

        /** forSeconds(5) == [DEFAULT]: 5*1000+350 = 5350, +500 = 5500, 5000. */
        fun forSeconds(seconds: Int): PanelTimeouts =
            PanelTimeouts(
                infoMs = seconds * MS_PER_SECOND + INFO_EXTRA_MS,
                zapMs = seconds * MS_PER_SECOND + ZAP_EXTRA_MS,
                quickBarMs = seconds * MS_PER_SECOND,
            )
    }
}
