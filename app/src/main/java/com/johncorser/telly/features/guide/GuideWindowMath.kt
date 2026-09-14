package com.johncorser.telly.features.guide

/** A half-open time span [fromMs, toMs) the grid materializes cells for. */
data class GuideSpan(
    val fromMs: Long,
    val toMs: Long,
)

/**
 * Which slice of time to materialize for a given horizontal scroll: the
 * visible window plus a prefetch margin, quantized to the origin's 30-min
 * grid so the Room window query only re-runs on real span changes.
 */
object GuideWindowMath {
    /** Prefetch margin on both sides of the visible window. */
    const val PREFETCH_MS = 3L * 60L * 60_000L

    /** Forward navigation limit; typical EPG horizon (catalogue fixtures). */
    const val FORWARD_DAYS = 7

    fun visibleSpan(
        originMs: Long,
        scrollXDp: Float,
        viewportDp: Float,
    ): GuideSpan =
        GuideSpan(
            fromMs = GuideGeometry.timeAt(scrollXDp, originMs),
            toMs = GuideGeometry.timeAt(scrollXDp + viewportDp, originMs),
        )

    fun materializeSpan(
        originMs: Long,
        scrollXDp: Float,
        viewportDp: Float,
    ): GuideSpan {
        val visible = visibleSpan(originMs, scrollXDp, viewportDp)
        return GuideSpan(
            fromMs = quantizeDown(visible.fromMs - PREFETCH_MS, originMs),
            toMs = quantizeUp(visible.toMs + PREFETCH_MS, originMs),
        )
    }

    /** Lowest allowed scroll: "Past days to keep EPG" back from the origin. */
    fun scrollFloorDp(pastDays: Int): Float = -pastDays * GuideGeometry.DAY_MS * GuideGeometry.DP_PER_MS

    fun scrollCeilDp(): Float = FORWARD_DAYS * GuideGeometry.DAY_MS * GuideGeometry.DP_PER_MS

    /** Floors [timeMs] onto the origin's 30-min grid. */
    fun quantizeDown(
        timeMs: Long,
        originMs: Long,
    ): Long = originMs + Math.floorDiv(timeMs - originMs, GuideGeometry.HALF_HOUR_MS) * GuideGeometry.HALF_HOUR_MS

    private fun quantizeUp(
        timeMs: Long,
        originMs: Long,
    ): Long {
        val floored = quantizeDown(timeMs, originMs)
        return if (floored == timeMs) floored else floored + GuideGeometry.HALF_HOUR_MS
    }
}
