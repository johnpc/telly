package com.johncorser.telly.features.guide

import com.johncorser.telly.features.guide.GuideTestData.at
import com.johncorser.telly.features.guide.GuideTestData.cell
import com.johncorser.telly.features.guide.GuideTestData.nowMs
import com.johncorser.telly.features.guide.GuideTestData.originMs
import com.johncorser.telly.testutil.testChannel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Sheet-close focus restore: identity-keyed, consumed once (round5 punch list). */
class GuideFocusMemoryTest {
    private val engine = GuideFocusEngine(originMs, pastFloorDp = { GuideWindowMath.scrollFloorDp(7) })
    private var rows = threeRows()
    private val memory = GuideFocusMemory(engine) { rows }

    private fun row(
        id: Long,
        title: String,
    ): GuideRow =
        GuideRow(
            testChannel(id, id.toInt(), "Ch $id"),
            id.toInt(),
            listOf(cell(at(14, 30), at(15, 30), title), cell(at(15, 30), at(16, 30), "$title later")),
        )

    private fun threeRows(): List<GuideRow> = listOf(row(1, "A"), row(2, "B"), row(3, "C"))

    private fun focusedChannelId(): Long? = engine.focus.value?.let { rows[it.rowIndex].channel.id }

    @Test
    fun `restore re-finds the saved channel's row after the rows shift`() {
        engine.ensureFocus(rows, nowMs)
        engine.moveVertical(rows, +1)
        memory.save()

        // The playlist refreshes under the open sheet: channel 1 vanishes,
        // sliding channel 3 into the saved row INDEX.
        rows = listOf(row(2, "B"), row(3, "C"))
        engine.ensureFocus(rows, nowMs)
        memory.restore()

        assertEquals(2L, focusedChannelId())
        assertEquals(0, engine.focus.value?.rowIndex)
        assertEquals(0, engine.firstVisibleRow.value)
    }

    @Test
    fun `restore re-asserts the saved scroll and cell`() {
        engine.ensureFocus(rows, nowMs)
        engine.moveRight(rows)
        val savedFocus = engine.focus.value
        val savedScroll = engine.scrollX.value
        memory.save()

        engine.reset()
        memory.restore()

        assertEquals(savedFocus, engine.focus.value)
        assertEquals(savedScroll, engine.scrollX.value)
    }

    @Test
    fun `a vanished channel keeps the engine's fallback focus`() {
        engine.ensureFocus(rows, nowMs)
        engine.moveVertical(rows, +1)
        memory.save()

        rows = listOf(row(1, "A"), row(3, "C"))
        engine.ensureFocus(rows, nowMs)
        memory.restore()

        assertEquals(3L, focusedChannelId())
    }

    @Test
    fun `the snapshot is consumed by the first restore`() {
        engine.ensureFocus(rows, nowMs)
        engine.moveVertical(rows, +1)
        memory.save()
        memory.restore()

        engine.moveVertical(rows, +1)
        memory.restore()

        assertEquals(3L, focusedChannelId())
    }

    @Test
    fun `nothing is saved without a focused row`() {
        memory.save()
        memory.restore()

        assertNull(engine.focus.value)
    }
}
