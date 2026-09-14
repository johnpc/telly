package com.johncorser.telly.features.guide

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
        zone: TimeZone,
    ): List<GuideTick> {
        val windowStartMs = GuideGeometry.timeAt(scrollXDp, originMs)
        var tickMs = GuideWindowMath.quantizeDown(windowStartMs, originMs)
        if (tickMs < windowStartMs) tickMs += GuideGeometry.HALF_HOUR_MS
        val windowEndMs = GuideGeometry.timeAt(scrollXDp + viewportDp, originMs)
        val ticks = mutableListOf<GuideTick>()
        while (tickMs < windowEndMs) {
            ticks += GuideTick(GuideGeometry.xOf(tickMs, originMs) - scrollXDp, timeLabel(tickMs, zone))
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

    /** "02:30 PM" — the captured 12-hour label format. */
    fun timeLabel(
        atMs: Long,
        zone: TimeZone,
    ): String = SimpleDateFormat("hh:mm a", Locale.US).apply { timeZone = zone }.format(Date(atMs))
}
