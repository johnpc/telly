package com.johncorser.telly.features.guide

import java.util.TimeZone

/**
 * The guide grid's time↔x mapping, all in dp. The reference captures show
 * 320 px per 30 minutes at 1920×1080 (uidump 24 timeline labels), i.e.
 * 160 dp per half hour with the 2 px = 1 dp rule. The fixed left channel
 * column is 380 px = 190 dp wide and rows repeat at 78 px = 39 dp pitch.
 */
object GuideGeometry {
    const val DP_PER_30_MIN = 160f
    const val HALF_HOUR_MS = 30L * 60_000L
    const val DAY_MS = 24L * 60L * 60_000L

    /** Channel column width: cells start at x=380 px in uidump 24. */
    const val CHANNEL_COLUMN_DP = 190f

    /** 78 px row pitch (uidump 24 channel rows). */
    const val ROW_HEIGHT_DP = 39f

    /** Time viewport on the 960 dp reference screen: 1920-380 px = 770 dp. */
    const val TIME_VIEWPORT_DP = 770f

    /** Full rows visible below the header ((1080-468)/78 ≈ 7.8). */
    const val VISIBLE_ROWS = 7

    /** Grid top on the reference layout: 468 px = 234 dp. */
    const val GRID_TOP_DP = 234f

    /** Grid viewport height on the reference layout: 1080-468 px = 306 dp. */
    const val GRID_HEIGHT_DP = 306f

    const val DP_PER_MS: Float = DP_PER_30_MIN / HALF_HOUR_MS

    /** Floor to the wall-clock half hour in [zone]: the grid's time origin. */
    fun halfHourFloor(
        nowMs: Long,
        zone: TimeZone,
    ): Long {
        val localMs = nowMs + zone.getOffset(nowMs)
        return nowMs - localMs % HALF_HOUR_MS
    }

    /** X in dp (relative to the origin's x=0) where [timeMs] falls. */
    fun xOf(
        timeMs: Long,
        originMs: Long,
    ): Float = (timeMs - originMs) * DP_PER_MS

    /** Inverse of [xOf]: the instant rendered at [xDp]. */
    fun timeAt(
        xDp: Float,
        originMs: Long,
    ): Long = originMs + (xDp / DP_PER_MS).toLong()

    fun widthOf(
        startMs: Long,
        endMs: Long,
    ): Float = (endMs - startMs) * DP_PER_MS
}
