package com.johncorser.telly.features.guide

/** Top-left position for an overlay anchored under a grid cell, in dp. */
data class GuideAnchor(
    val xDp: Float,
    val yDp: Float,
)

/**
 * Positions the cell-action dropdown under its cell (capture 27: the menu
 * hangs from the focused cell, not centered), clamped so it never leaves
 * the grid viewport.
 */
object GuideDropdownAnchor {
    const val MENU_WIDTH_DP = 180f
    private const val MENU_HEIGHT_DP = 200f

    fun position(
        focus: GuideFocus,
        firstVisibleRow: Int,
        originMs: Long,
        scrollXDp: Float,
    ): GuideAnchor {
        val cellX = GuideGeometry.xOf(focus.cell.startMs, originMs) - scrollXDp
        val maxCellX = GuideGeometry.TIME_VIEWPORT_DP - MENU_WIDTH_DP
        val xDp = GuideGeometry.CHANNEL_COLUMN_DP + cellX.coerceIn(0f, maxCellX)
        val rowBottom = (focus.rowIndex - firstVisibleRow + 1) * GuideGeometry.ROW_HEIGHT_DP
        val yDp = rowBottom.coerceIn(0f, maxOf(GuideGeometry.GRID_HEIGHT_DP - MENU_HEIGHT_DP, 0f))
        return GuideAnchor(xDp = xDp, yDp = yDp)
    }
}
