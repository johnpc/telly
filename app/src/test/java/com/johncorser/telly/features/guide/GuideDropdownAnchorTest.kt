package com.johncorser.telly.features.guide

import com.johncorser.telly.features.guide.GuideTestData.at
import com.johncorser.telly.features.guide.GuideTestData.cell
import com.johncorser.telly.features.guide.GuideTestData.originMs
import org.junit.Assert.assertEquals
import org.junit.Test

class GuideDropdownAnchorTest {
    private val viewport = GuideGeometry.TIME_VIEWPORT_DP
    private val gridHeight = GuideGeometry.GRID_HEIGHT_DP

    private fun anchorFor(
        focus: GuideFocus,
        firstVisibleRow: Int = 0,
    ): GuideAnchor =
        GuideDropdownAnchor.position(
            focus = focus,
            firstVisibleRow = firstVisibleRow,
            originMs = originMs,
            scrollXDp = 0f,
        )

    @Test
    fun `the dropdown hangs from the focused cell's bottom-left corner`() {
        val focus = GuideFocus(rowIndex = 1, cell = cell(at(15, 45), at(17, 0)), anchorMs = at(15, 45))

        val anchor = anchorFor(focus)

        assertEquals(GuideGeometry.CHANNEL_COLUMN_DP + 400f, anchor.xDp)
        assertEquals(2 * GuideGeometry.ROW_HEIGHT_DP, anchor.yDp)
    }

    @Test
    fun `the anchor tracks the scrolled row window`() {
        val focus = GuideFocus(rowIndex = 6, cell = cell(at(14, 30), at(15, 0)), anchorMs = at(14, 38))

        val anchor = anchorFor(focus, firstVisibleRow = 5)

        assertEquals(2 * GuideGeometry.ROW_HEIGHT_DP, anchor.yDp)
    }

    @Test
    fun `the anchor clamps inside the grid viewport`() {
        val farRight = GuideFocus(rowIndex = 20, cell = cell(at(17, 30), at(18, 0)), anchorMs = at(17, 30))

        val anchor = anchorFor(farRight, firstVisibleRow = 14)

        assertEquals(GuideGeometry.CHANNEL_COLUMN_DP + viewport - GuideDropdownAnchor.MENU_WIDTH_DP, anchor.xDp)
        assertEquals(gridHeight - 200f, anchor.yDp)
    }
}
