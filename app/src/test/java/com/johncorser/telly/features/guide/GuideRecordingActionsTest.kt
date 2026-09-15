package com.johncorser.telly.features.guide

import com.johncorser.telly.features.recording.FakeRecordingDao
import com.johncorser.telly.features.recording.RecordingCenter
import com.johncorser.telly.features.recording.RecordingEngine
import com.johncorser.telly.features.recording.RecordingMenu
import com.johncorser.telly.features.recording.RecordingScheduler
import com.johncorser.telly.features.recording.RecordingStatus
import com.johncorser.telly.features.recording.RecordingStore
import com.johncorser.telly.features.recording.idleRecorder
import com.johncorser.telly.features.recording.recordingStatus
import com.johncorser.telly.features.recording.tempRecordingFiles
import com.johncorser.telly.testutil.testChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GuideRecordingActionsTest {
    private val dao = FakeRecordingDao()
    private var now = 1_000L
    private val store = RecordingStore(dao) { now }
    private val files = tempRecordingFiles().second
    private val row = GuideRow(testChannel(1, 1, "News One"), 1, emptyList())
    private val shown = mutableListOf<GuideLayer>()

    private fun TestScope.actions(withMenu: Boolean = true): GuideRecordingActions {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val menu: RecordingMenu? =
            if (withMenu) {
                val engine = RecordingEngine(store, idleRecorder(), files, scope, { now })
                val scheduler = RecordingScheduler(store, engine, { now }, scope, MutableSharedFlow())
                val center = RecordingCenter(store, engine, scheduler, files, { _, _ -> null }, { now })
                RecordingMenu(center, scope, { now }) { }
            } else {
                null
            }
        return GuideRecordingActions({ menu }, { row }) { shown += it }
    }

    @Test
    fun `recordCell schedules the focused future programme's slot`() =
        runTest(UnconfinedTestDispatcher()) {
            val cell = GuideTestData.cell(GuideTestData.at(15, 0), GuideTestData.at(16, 0), title = "Politics Tonight")

            actions().recordCell(GuideLayer.CellMenu(cell))

            val entry = dao.rows.value.single()
            assertEquals(RecordingStatus.SCHEDULED, entry.recordingStatus)
            assertEquals(GuideTestData.at(15, 0), entry.startMs)
        }

    @Test
    fun `recordCell with no DVR wired falls back to coming-soon`() =
        runTest(UnconfinedTestDispatcher()) {
            val cell = GuideTestData.cell(GuideTestData.at(15, 0), GuideTestData.at(16, 0))

            actions(withMenu = false).recordCell(GuideLayer.CellMenu(cell))

            assertEquals(GuideLayer.ComingSoon(GuideCellAction.RECORD.label), shown.single())
            assertTrue(dao.rows.value.isEmpty())
        }

    @Test
    fun `record starts an instant capture of the row's channel`() =
        runTest(UnconfinedTestDispatcher()) {
            actions().record(row.channel)

            assertEquals("News One", dao.rows.value.single().channelName)
        }

    @Test
    fun `customRecording with no DVR wired falls back to coming-soon`() =
        runTest(UnconfinedTestDispatcher()) {
            actions(withMenu = false).customRecording()

            assertEquals(GuideLayer.ComingSoon(GuideCellAction.CUSTOM_RECORDING.label), shown.single())
        }
}
