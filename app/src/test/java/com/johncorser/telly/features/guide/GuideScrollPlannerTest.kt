package com.johncorser.telly.features.guide

import com.johncorser.telly.features.guide.GuideTestData.at
import com.johncorser.telly.features.guide.GuideTestData.cell
import com.johncorser.telly.features.guide.GuideTestData.originMs
import org.junit.Assert.assertEquals
import org.junit.Test

class GuideScrollPlannerTest {
    private val viewport = GuideGeometry.TIME_VIEWPORT_DP

    @Test
    fun `a fully visible cell keeps the scroll still`() {
        val visible = cell(at(15, 0), at(15, 30))

        assertEquals(0f, GuideScrollPlanner.horizontalTarget(visible, originMs, 0f, viewport))
    }

    @Test
    fun `a cell past the right edge scrolls its start into view`() {
        // 17:30 starts at x=960 > viewport 690: align its end when it fits.
        val ahead = cell(at(17, 30), at(18, 0))

        val target = GuideScrollPlanner.horizontalTarget(ahead, originMs, 0f, viewport)

        assertEquals(GuideGeometry.xOf(at(18, 0), originMs) - viewport, target)
    }

    @Test
    fun `a cell wider than the viewport aligns to its start`() {
        val movie = cell(at(17, 30), at(23, 30))

        val target = GuideScrollPlanner.horizontalTarget(movie, originMs, 0f, viewport)

        assertEquals(GuideGeometry.xOf(at(17, 30), originMs), target)
    }

    @Test
    fun `a cell left of the window aligns its start to the left edge`() {
        val behind = cell(at(14, 30), at(15, 0))

        val target = GuideScrollPlanner.horizontalTarget(behind, originMs, 480f, viewport)

        assertEquals(0f, target)
    }

    @Test
    fun `the row window follows focus down and up`() {
        assertEquals(0, GuideScrollPlanner.rowWindow(0, 0, 7, 30))
        assertEquals(0, GuideScrollPlanner.rowWindow(0, 6, 7, 30))
        assertEquals(1, GuideScrollPlanner.rowWindow(0, 7, 7, 30))
        assertEquals(4, GuideScrollPlanner.rowWindow(4, 10, 7, 30))
        assertEquals(3, GuideScrollPlanner.rowWindow(4, 3, 7, 30))
    }

    @Test
    fun `the row window clamps to the list bounds`() {
        assertEquals(0, GuideScrollPlanner.rowWindow(0, 2, 7, 3))
        assertEquals(23, GuideScrollPlanner.rowWindow(25, 29, 7, 30))
    }
}
