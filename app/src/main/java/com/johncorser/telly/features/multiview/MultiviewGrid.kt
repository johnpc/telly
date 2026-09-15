package com.johncorser.telly.features.multiview

/** One pane cell as screen fractions; panes letterbox 16:9 inside their cell. */
data class PaneCell(
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
)

/**
 * Multiview mosaic geometry. The free reference only fixes the 1-pane
 * framing (multiview-round 03: a centered half-size pane, [480,270][1440,810]
 * on 1920x1080); the 2/3/4-pane grids are premium and uncapturable, so they
 * are telly design decisions (see the decisions log): 2 = side-by-side
 * halves, 3 = one large pane left + two stacked right (no dead cell),
 * 4 = 2x2 quadrants. The pane menu sits right of the focused pane's cell,
 * vertically centered on it (multiview-round 04: 200 dp wide, 40 dp rows,
 * 20 dp off the pane edge), clamped inside the screen.
 */
object MultiviewGrid {
    const val MAX_PANES = 4

    private const val THREE_PANES = 3
    private const val HALF = 0.5f
    private const val QUARTER = 0.25f
    private const val TWO_THIRDS = 2f / 3f
    private const val THIRD = 1f / 3f

    const val MENU_WIDTH_DP = 200f
    const val MENU_ROW_DP = 40f
    private const val MENU_GAP_DP = 20f
    private const val MENU_EDGE_DP = 16f

    fun cells(count: Int): List<PaneCell> =
        when (count) {
            2 -> listOf(PaneCell(0f, 0f, HALF, 1f), PaneCell(HALF, 0f, HALF, 1f))
            THREE_PANES ->
                listOf(
                    PaneCell(0f, 0f, TWO_THIRDS, 1f),
                    PaneCell(TWO_THIRDS, 0f, THIRD, HALF),
                    PaneCell(TWO_THIRDS, HALF, THIRD, HALF),
                )
            MAX_PANES ->
                listOf(
                    PaneCell(0f, 0f, HALF, HALF),
                    PaneCell(HALF, 0f, HALF, HALF),
                    PaneCell(0f, HALF, HALF, HALF),
                    PaneCell(HALF, HALF, HALF, HALF),
                )
            else -> listOf(PaneCell(QUARTER, QUARTER, HALF, HALF))
        }

    /** Top-left of the [rowCount]-row pane menu, in dp on a [screenW]x[screenH] dp screen. */
    fun menuOffset(
        cell: PaneCell,
        screenW: Float,
        screenH: Float,
        rowCount: Int,
    ): Pair<Float, Float> {
        val menuH = rowCount * MENU_ROW_DP
        val x = ((cell.x + cell.w) * screenW + MENU_GAP_DP).coerceAtMost(screenW - MENU_WIDTH_DP - MENU_EDGE_DP)
        val y = ((cell.y + cell.h / 2f) * screenH - menuH / 2f).coerceIn(MENU_EDGE_DP, screenH - menuH - MENU_EDGE_DP)
        return x to y
    }
}
