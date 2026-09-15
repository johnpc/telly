package com.johncorser.telly.features.multiview

import org.junit.Assert.assertEquals
import org.junit.Test

class MultiviewGridTest {
    @Test
    fun `one pane is the captured centered half-size teaser framing`() {
        // multiview-round 03: pane [480,270][1440,810] on 1920x1080.
        assertEquals(listOf(PaneCell(0.25f, 0.25f, 0.5f, 0.5f)), MultiviewGrid.cells(1))
        assertEquals(MultiviewGrid.cells(1), MultiviewGrid.cells(0))
    }

    @Test
    fun `two panes are side-by-side halves`() {
        assertEquals(
            listOf(PaneCell(0f, 0f, 0.5f, 1f), PaneCell(0.5f, 0f, 0.5f, 1f)),
            MultiviewGrid.cells(2),
        )
    }

    @Test
    fun `three panes are one large left plus two stacked right`() {
        val cells = MultiviewGrid.cells(3)
        assertEquals(PaneCell(0f, 0f, 2f / 3f, 1f), cells[0])
        assertEquals(PaneCell(2f / 3f, 0f, 1f / 3f, 0.5f), cells[1])
        assertEquals(PaneCell(2f / 3f, 0.5f, 1f / 3f, 0.5f), cells[2])
    }

    @Test
    fun `four panes are a 2x2 quad`() {
        val cells = MultiviewGrid.cells(4)
        assertEquals(4, cells.size)
        assertEquals(PaneCell(0.5f, 0.5f, 0.5f, 0.5f), cells[3])
        // Every quadrant keeps the screen's own 16:9 ratio — no letterbox.
        cells.forEach { assertEquals(it.w, it.h, 0f) }
    }

    @Test
    fun `the single-pane menu sits right of the pane, vertically centered on it`() {
        // multiview-round 04: menu [1480,420][1880,660] = 740,210 dp for
        // three 40 dp rows on a 960x540 dp screen.
        val (x, y) = MultiviewGrid.menuOffset(MultiviewGrid.cells(1).first(), 960f, 540f, rowCount = 3)
        assertEquals(740f, x, 0.01f)
        assertEquals(210f, y, 0.01f)
    }

    @Test
    fun `the menu clamps inside the screen when the pane touches the right edge`() {
        val rightHalf = MultiviewGrid.cells(2)[1]
        val (x, y) = MultiviewGrid.menuOffset(rightHalf, 960f, 540f, rowCount = 4)
        assertEquals(960f - MultiviewGrid.MENU_WIDTH_DP - 16f, x, 0.01f)
        assertEquals(270f - 2 * MultiviewGrid.MENU_ROW_DP, y, 0.01f)
    }
}
