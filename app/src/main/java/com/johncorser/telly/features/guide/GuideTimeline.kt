package com.johncorser.telly.features.guide

import com.johncorser.telly.features.playback.ClockStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** One 30-min timeline label, positioned relative to the grid's left edge. */
data class GuideTick(
    val offsetDp: Float,
    val label: String,
)

/**
 * The scrolling timeline header (uidump 24: "02:30 PM" labels every 320 px)
 * and the thin "now" rule that runs down the grid.
 */
object GuideTimeline {
    fun ticks(
        originMs: Long,
        scrollXDp: Float,
        viewportDp: Float,
        style: ClockStyle,
    ): List<GuideTick> {
        val windowStartMs = GuideGeometry.timeAt(scrollXDp, originMs)
        var tickMs = GuideWindowMath.quantizeDown(windowStartMs, originMs)
        if (tickMs < windowStartMs) tickMs += GuideGeometry.HALF_HOUR_MS
        val windowEndMs = GuideGeometry.timeAt(scrollXDp + viewportDp, originMs)
        val ticks = mutableListOf<GuideTick>()
        while (tickMs < windowEndMs) {
            ticks += GuideTick(GuideGeometry.xOf(tickMs, originMs) - scrollXDp, timeLabel(tickMs, style))
            tickMs += GuideGeometry.HALF_HOUR_MS
        }
        return ticks
    }

    /** X of the now-line inside the grid viewport; null when off screen. */
    fun nowLineOffset(
        nowMs: Long,
        originMs: Long,
        scrollXDp: Float,
        viewportDp: Float,
    ): Float? {
        val offset = GuideGeometry.xOf(nowMs, originMs) - scrollXDp
        return offset.takeIf { it in 0f..viewportDp }
    }

    /** "02:30 PM" (the captured default) or "14:30" in 24-hour format. */
    fun timeLabel(
        atMs: Long,
        style: ClockStyle,
    ): String =
        SimpleDateFormat(if (style.is24h) "HH:mm" else "hh:mm a", Locale.US)
            .apply { timeZone = style.zone }
            .format(Date(atMs))
}
