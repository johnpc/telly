package com.johncorser.telly.features.guide

import com.johncorser.telly.features.guide.GuideTestData.at
import com.johncorser.telly.features.guide.GuideTestData.nowMs
import com.johncorser.telly.features.guide.GuideTestData.originMs
import com.johncorser.telly.features.playback.TuneController
import com.johncorser.telly.testutil.FakeKeyValueStore
import com.johncorser.telly.testutil.testChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GuideFocusResumeTest {
    private val store = FakeKeyValueStore()
    private val engine = GuideFocusEngine(originMs, { 0f }, visibleRows = { 2 })

    private fun row(id: Long): GuideRow =
        GuideRow(
            channel = testChannel(id, id.toInt(), "Channel $id"),
            displayNumber = id.toInt(),
            cells = listOf(GuideTestData.cell(at(14, 30), at(15, 0))),
        )

    private fun rows(count: Int): List<GuideRow> = (1..count).map { row(it.toLong()) }

    private fun TestScope.arm(
        resume: GuideFocusResume,
        flow: MutableStateFlow<List<GuideRow>>,
    ) = resume.arm(CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler)), flow) { nowMs }

    @Test
    fun `focuses the stored channel's row and scrolls its window into view`() {
        runTest {
            store.putLong(TuneController.LAST_CHANNEL_KEY, 4L)

            arm(GuideFocusResume(engine, store) { 2 }, MutableStateFlow(rows(4)))

            assertEquals(3, engine.focus.value?.rowIndex)
            assertEquals(nowMs, engine.focus.value?.anchorMs)
            assertEquals(2, engine.firstVisibleRow.value)
        }
    }

    @Test
    fun `waits for the first non-empty rows emission`() {
        runTest {
            store.putLong(TuneController.LAST_CHANNEL_KEY, 3L)
            val flow = MutableStateFlow(emptyList<GuideRow>())

            arm(GuideFocusResume(engine, store) { 2 }, flow)
            assertNull(engine.focus.value)

            flow.value = rows(4)
            assertEquals(2, engine.focus.value?.rowIndex)
        }
    }

    @Test
    fun `a stored channel missing from the rows keeps the default focus`() {
        runTest {
            store.putLong(TuneController.LAST_CHANNEL_KEY, 99L)

            arm(GuideFocusResume(engine, store) { 2 }, MutableStateFlow(rows(4)))

            assertNull(engine.focus.value)
            assertEquals(0, engine.firstVisibleRow.value)
        }
    }

    @Test
    fun `the first row keeps the engine's own initial resolution`() {
        runTest {
            store.putLong(TuneController.LAST_CHANNEL_KEY, 1L)

            arm(GuideFocusResume(engine, store) { 2 }, MutableStateFlow(rows(4)))

            assertNull(engine.focus.value)
        }
    }

    @Test
    fun `no stored channel is a no-op`() {
        runTest {
            arm(GuideFocusResume(engine, store) { 2 }, MutableStateFlow(rows(4)))

            assertNull(engine.focus.value)
        }
    }
}
