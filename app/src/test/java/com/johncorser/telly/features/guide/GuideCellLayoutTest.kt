package com.johncorser.telly.features.guide

import com.johncorser.telly.features.guide.GuideTestData.at
import com.johncorser.telly.features.guide.GuideTestData.cell
import com.johncorser.telly.features.guide.GuideTestData.originMs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GuideCellLayoutTest {
    private val viewport = GuideGeometry.TIME_VIEWPORT_DP

    @Test
    fun `a cell inside the window renders at its true x and width`() {
        val placement = GuideCellLayout.place(cell(at(14, 45), at(15, 45)), originMs, 0f, viewport)!!

        assertEquals(80f, placement.offsetDp)
        assertEquals(320f, placement.widthDp)
        assertFalse(placement.clipped)
    }

    @Test
    fun `a running programme clamps to the window's left edge`() {
        // Started 14:10, window starts 14:30 (uidump 24 row 2).
        val placement = GuideCellLayout.place(cell(at(14, 10), at(15, 30)), originMs, 0f, viewport)!!

        assertEquals(0f, placement.offsetDp)
        assertEquals(320f, placement.widthDp)
        assertTrue(placement.clipped)
    }

    @Test
    fun `cells outside the window do not materialize`() {
        assertNull(GuideCellLayout.place(cell(at(13, 0), at(14, 30)), originMs, 0f, viewport))
        assertNull(GuideCellLayout.place(cell(at(19, 0), at(20, 0)), originMs, 0f, viewport))
    }

    @Test
    fun `a long programme clamps its width to the viewport`() {
        val placement = GuideCellLayout.place(cell(at(14, 0), at(23, 0)), originMs, 0f, viewport)!!

        assertEquals(0f, placement.offsetDp)
        assertEquals(viewport, placement.widthDp)
    }

    @Test
    fun `a sliver keeps the minimum legible width`() {
        val placement = GuideCellLayout.place(cell(at(14, 30), at(14, 31)), originMs, 0f, viewport)!!

        assertEquals(GuideCellLayout.MIN_CELL_DP, placement.widthDp)
    }
}
