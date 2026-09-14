package com.johncorser.telly.features.guide

/** Where a cell renders inside the row viewport, after clamping. */
data class GuideCellPlacement(
    val offsetDp: Float,
    val widthDp: Float,
    val clipped: Boolean,
)

/**
 * Cell layout math: startMs→x at 160 dp per 30 min, cells that started
 * before the window clamp to its left edge (uidump 24 row 2: a running
 * programme renders from the column edge x=540 with its label at the
 * visible edge), and widths clamp to the viewport so a long programme
 * can't blow the row up.
 */
object GuideCellLayout {
    /** Smallest rendered width so a sliver of cell stays visible/legible. */
    const val MIN_CELL_DP = 8f

    fun place(
        cell: GuideCell,
        originMs: Long,
        scrollXDp: Float,
        viewportDp: Float,
    ): GuideCellPlacement? {
        val startX = GuideGeometry.xOf(cell.startMs, originMs) - scrollXDp
        val endX = GuideGeometry.xOf(cell.endMs, originMs) - scrollXDp
        if (endX <= 0f || startX >= viewportDp) return null
        val left = maxOf(startX, 0f)
        val right = minOf(endX, viewportDp)
        val width = maxOf(right - left, MIN_CELL_DP)
        return GuideCellPlacement(offsetDp = left, widthDp = width, clipped = startX < 0f)
    }
}
