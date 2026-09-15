package com.johncorser.telly.features.vod

import java.util.Locale

/** Transport clock texts: "m:ss" under an hour, "h:mm:ss" above. */
object VodTimes {
    private const val SECOND_MS = 1000L
    private const val MINUTE_SECONDS = 60L
    private const val HOUR_SECONDS = 3600L

    fun format(ms: Long): String {
        val total = (ms / SECOND_MS).coerceAtLeast(0)
        val hours = total / HOUR_SECONDS
        val minutes = total % HOUR_SECONDS / MINUTE_SECONDS
        val seconds = total % MINUTE_SECONDS
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%d:%02d", minutes, seconds)
        }
    }
}
