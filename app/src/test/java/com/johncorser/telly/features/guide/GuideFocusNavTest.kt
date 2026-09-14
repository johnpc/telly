package com.johncorser.telly.features.guide

import com.johncorser.telly.features.guide.GuideTestData.at
import com.johncorser.telly.features.guide.GuideTestData.cell
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GuideFocusNavTest {
    private val cells =
        listOf(
            cell(at(14, 10), at(14, 35), "A"),
            cell(at(14, 35), at(15, 30), "B"),
            cell(at(15, 30), at(16, 30), "C"),
        )

    @Test
    fun `right moves to the next programme and stops at the strip end`() {
        assertEquals("B", GuideFocusNav.rightOf(cells, cells[0])?.program?.details?.title)
        assertNull(GuideFocusNav.rightOf(cells, cells[2]))
    }

    @Test
    fun `left moves back while the focus is right of the window edge`() {
        val windowStartMs = at(14, 30)

        assertEquals("B", GuideFocusNav.leftOf(cells, cells[2], windowStartMs)?.program?.details?.title)
        assertEquals("A", GuideFocusNav.leftOf(cells, cells[1], windowStartMs)?.program?.details?.title)
    }

    @Test
    fun `left at the window edge yields null so the groups column opens`() {
        // "A" started before the window: it is the flush-left cell (capture 25).
        assertNull(GuideFocusNav.leftOf(cells, cells[0], at(14, 30)))
    }

    @Test
    fun `cellAt picks the cell containing the anchor`() {
        assertEquals("B", GuideFocusNav.cellAt(cells, at(15, 0))?.program?.details?.title)
        assertEquals("A", GuideFocusNav.cellAt(cells, at(14, 10))?.program?.details?.title)
    }

    @Test
    fun `cellAt clamps to the nearest edge outside the strip`() {
        assertEquals("A", GuideFocusNav.cellAt(cells, at(13, 0))?.program?.details?.title)
        assertEquals("C", GuideFocusNav.cellAt(cells, at(18, 0))?.program?.details?.title)
        assertNull(GuideFocusNav.cellAt(emptyList(), at(15, 0)))
    }

    @Test
    fun `an unknown focused cell cannot move`() {
        val stranger = cell(at(20, 0), at(21, 0))

        assertNull(GuideFocusNav.rightOf(cells, stranger))
        assertNull(GuideFocusNav.leftOf(cells, stranger, at(14, 30)))
    }
}
