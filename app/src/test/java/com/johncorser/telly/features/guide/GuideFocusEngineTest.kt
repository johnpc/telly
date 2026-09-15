package com.johncorser.telly.features.guide

import com.johncorser.telly.features.guide.GuideTestData.at
import com.johncorser.telly.features.guide.GuideTestData.cell
import com.johncorser.telly.features.guide.GuideTestData.nowMs
import com.johncorser.telly.features.guide.GuideTestData.originMs
import com.johncorser.telly.testutil.testChannel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GuideFocusEngineTest {
    private val engine = GuideFocusEngine(originMs, pastFloorDp = { GuideWindowMath.scrollFloorDp(7) })

    private fun row(
        id: Long,
        cells: List<GuideCell>,
    ): GuideRow = GuideRow(testChannel(id, id.toInt(), "Ch $id"), id.toInt(), cells)

    private val rows =
        listOf(
            row(1, listOf(cell(at(14, 30), at(15, 45), "A1"), cell(at(15, 45), at(17, 15), "A2"))),
            row(
                2,
                listOf(
                    cell(at(14, 10), at(15, 30), "B1"),
                    cell(at(15, 30), at(16, 0), "B2"),
                    cell(at(16, 0), at(18, 0), "B3"),
                ),
            ),
        )

    private fun title(focus: GuideFocus?): String? = focus?.cell?.program?.details?.title

    @Test
    fun `focus initializes on the first row's airing cell`() {
        engine.ensureFocus(rows, nowMs)

        assertEquals(0, engine.focus.value?.rowIndex)
        assertEquals("A1", title(engine.focus.value))
        assertEquals(0f, engine.scrollX.value)
    }

    @Test
    fun `right moves along the row and pans once the cell leaves the window`() {
        engine.ensureFocus(rows, nowMs)

        engine.moveRight(rows)

        assertEquals("A2", title(engine.focus.value))
        // A2 ends at 17:15 = x 880 > the 690 viewport: the window pans right.
        assertEquals(880f - GuideGeometry.TIME_VIEWPORT_DP, engine.scrollX.value)
    }

    @Test
    fun `left walks back and reports the edge for the groups column`() {
        engine.ensureFocus(rows, nowMs)
        engine.moveRight(rows)

        assertTrue(engine.moveLeft(rows))
        assertEquals("A1", title(engine.focus.value))
        assertFalse(engine.moveLeft(rows))
    }

    @Test
    fun `plain navigation never pans left of now`() {
        engine.ensureFocus(rows, nowMs)
        engine.moveRight(rows)

        engine.moveLeft(rows)

        assertEquals(0f, engine.scrollX.value)
    }

    @Test
    fun `vertical moves keep the focused time constant`() {
        engine.ensureFocus(rows, nowMs)
        engine.moveRight(rows)
        val anchorMs = engine.focus.value!!.anchorMs

        engine.moveVertical(rows, +1)

        assertEquals(1, engine.focus.value?.rowIndex)
        assertEquals(anchorMs, engine.focus.value?.anchorMs)
        assertTrue(engine.focus.value!!.cell.contains(anchorMs))

        engine.moveVertical(rows, -1)
        assertEquals("A2", title(engine.focus.value))
    }

    @Test
    fun `vertical moves clamp at the list edges`() {
        engine.ensureFocus(rows, nowMs)

        engine.moveVertical(rows, -1)

        assertEquals(0, engine.focus.value?.rowIndex)
    }

    @Test
    fun `right at the materialized edge pans the window forward`() {
        engine.ensureFocus(rows, nowMs)
        engine.moveVertical(rows, +1)
        engine.moveRight(rows)
        engine.moveRight(rows)
        val scrollBefore = engine.scrollX.value

        engine.moveRight(rows)

        assertEquals("B3", title(engine.focus.value))
        assertEquals(scrollBefore + GuideGeometry.DP_PER_30_MIN, engine.scrollX.value)
    }

    @Test
    fun `a day jump moves the anchor and aligns the window to the grid`() {
        engine.ensureFocus(rows, nowMs)

        engine.dayJump(days = -1, maxPastDays = 7)

        assertEquals(nowMs - GuideGeometry.DAY_MS, engine.focus.value?.anchorMs)
        assertEquals(GuideGeometry.xOf(at(14, 30, dayOffset = -1), originMs), engine.scrollX.value)
    }

    @Test
    fun `day jumps clamp to the past-days floor`() {
        engine.ensureFocus(rows, nowMs)

        engine.dayJump(days = -1, maxPastDays = 0)

        assertEquals(0f, engine.scrollX.value)
        assertEquals(GuideGeometry.timeAt(0f, originMs), engine.focus.value?.anchorMs)
    }

    @Test
    fun `once in the past plain moves may pan until the floor`() {
        engine.ensureFocus(rows, nowMs)
        engine.dayJump(days = -1, maxPastDays = 7)
        val pastRows =
            listOf(
                row(1, listOf(cell(at(13, 0, -1), at(14, 35, -1), "Y1"), cell(at(14, 35, -1), at(16, 0, -1), "Y2"))),
                rows[1],
            )
        engine.ensureFocus(pastRows, nowMs)

        assertEquals("Y2", title(engine.focus.value))
        assertTrue(engine.moveLeft(pastRows))
        assertEquals("Y1", title(engine.focus.value))
        assertTrue(engine.scrollX.value < 0f)
    }

    @Test
    fun `a page jump pans a whole viewport and shifts the anchor with it`() {
        engine.ensureFocus(rows, nowMs)
        val anchorBefore = engine.focus.value!!.anchorMs

        assertTrue(engine.pageJump(rows, +1))

        assertEquals(GuideGeometry.TIME_VIEWPORT_DP, engine.scrollX.value)
        val expectedAnchor = anchorBefore + GuideGeometry.timeAt(GuideGeometry.TIME_VIEWPORT_DP, 0L)
        assertEquals(expectedAnchor, engine.focus.value?.anchorMs)

        assertTrue(engine.pageJump(rows, -1))
        assertEquals(0f, engine.scrollX.value)
        assertEquals(anchorBefore, engine.focus.value?.anchorMs)
        assertEquals("A1", title(engine.focus.value))
    }

    @Test
    fun `a page jump left at the live edge reports the edge for the groups column`() {
        engine.ensureFocus(rows, nowMs)

        assertFalse(engine.pageJump(rows, -1))
        assertEquals(0f, engine.scrollX.value)
    }

    @Test
    fun `page jumps land on the cell at the new anchor when it is materialized`() {
        engine.ensureFocus(rows, nowMs)
        val longRow =
            listOf(row(1, listOf(rows[0].cells[0], cell(at(15, 45), at(20, 0), "A2 Marathon"))))
        engine.ensureFocus(longRow, nowMs)

        assertTrue(engine.pageJump(longRow, +1))

        assertEquals("A2 Marathon", title(engine.focus.value))
    }

    @Test
    fun `reset clears focus and scroll for a group switch`() {
        engine.ensureFocus(rows, nowMs)
        engine.moveRight(rows)

        engine.reset()

        assertNull(engine.focus.value)
        assertEquals(0f, engine.scrollX.value)
        assertEquals(0, engine.firstVisibleRow.value)
    }

    @Test
    fun `empty rows clear the focus`() {
        engine.ensureFocus(rows, nowMs)

        engine.ensureFocus(emptyList(), nowMs)

        assertNull(engine.focus.value)
    }
}
