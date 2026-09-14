package com.johncorser.telly.features.guide

/**
 * Where the grid must scroll so the focused cell is visible: the timeline
 * header and every row pan together off one horizontal offset, and the
 * channel list scrolls by whole rows.
 */
object GuideScrollPlanner {
    /**
     * Horizontal scroll (dp from the origin) keeping [cell] in view: cells
     * left of the window align their start to the left edge; cells running
     * off the right edge show their start plus as much body as fits.
     */
    fun horizontalTarget(
        cell: GuideCell,
        originMs: Long,
        scrollXDp: Float,
        viewportDp: Float,
    ): Float {
        val startX = GuideGeometry.xOf(cell.startMs, originMs)
        val endX = GuideGeometry.xOf(cell.endMs, originMs)
        return when {
            startX < scrollXDp -> startX
            endX > scrollXDp + viewportDp -> minOf(startX, endX - viewportDp)
            else -> scrollXDp
        }
    }

    /** First visible row index keeping [focusRow] inside the row window. */
    fun rowWindow(
        firstVisible: Int,
        focusRow: Int,
        visibleRows: Int,
        rowCount: Int,
    ): Int {
        val maxFirst = (rowCount - visibleRows).coerceAtLeast(0)
        val pulledUp = minOf(firstVisible, focusRow)
        val pulledDown = maxOf(pulledUp, focusRow - visibleRows + 1)
        return pulledDown.coerceIn(0, maxFirst)
    }
}
