package com.johncorser.telly.features.search

import com.johncorser.telly.features.mylist.InMemoryMyListStore
import com.johncorser.telly.features.mylist.MyListKeys
import com.johncorser.telly.features.recording.FakeRecordingDao
import com.johncorser.telly.features.recording.RecordingCenter
import com.johncorser.telly.features.recording.RecordingEngine
import com.johncorser.telly.features.recording.RecordingScheduler
import com.johncorser.telly.features.recording.RecordingStatus
import com.johncorser.telly.features.recording.RecordingStore
import com.johncorser.telly.features.recording.RecordingSupport
import com.johncorser.telly.features.recording.oneShotRecorder
import com.johncorser.telly.features.recording.recordingStatus
import com.johncorser.telly.features.recording.tempRecordingFiles
import com.johncorser.telly.features.reminders.FakeReminderDao
import com.johncorser.telly.features.reminders.GuideReminders
import com.johncorser.telly.features.reminders.ReminderStore
import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.testProgram
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The search dropdown drives the SAME stores as the guide's cell dropdown. */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchProgramMenuTest {
    private val now = 1_000L
    private val channel = testChannel(1, 1, "News One")
    private val program = testProgram("tvg-1", 5_000L, 6_000L, "Politics Tonight")
    private val hit = SearchProgramHit(program, channel, "Politics Tonight", "05:00", 0, null)
    private val overlays = SearchOverlays()
    private val reminderDao = FakeReminderDao()
    private val myListStore = InMemoryMyListStore()
    private val recordingDao = FakeRecordingDao()

    private fun TestScope.menu(hooks: SearchHooks = liveHooks()): SearchProgramMenu =
        SearchProgramMenu(hooks, overlays, { now }, CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

    private fun TestScope.liveHooks(): SearchHooks {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val store = RecordingStore(recordingDao) { now }
        val files = tempRecordingFiles().second
        val engine = RecordingEngine(store, oneShotRecorder(), files, scope, { now })
        val scheduler = RecordingScheduler(store, engine, { now }, scope, MutableSharedFlow())
        return SearchHooks(
            reminders = GuideReminders(ReminderStore(reminderDao), scope),
            myList = myListStore,
            recording = RecordingCenter(store, engine, scheduler, files, { _, _ -> null }, { now }),
        )
    }

    @Test
    fun `remind toggles the reminder store and the label flips`() =
        runTest {
            val menu = menu()
            assertEquals("Remind", menu.label(SearchProgramAction.REMIND, hit, menu.reminderKeys.value, emptySet()))

            menu.onAction(SearchProgramAction.REMIND, hit)

            assertEquals(SearchOverlay.None, overlays.current.value)
            assertEquals(
                "Remove reminder",
                menu.label(SearchProgramAction.REMIND, hit, menu.reminderKeys.value, emptySet()),
            )

            menu.onAction(SearchProgramAction.REMIND, hit)
            assertEquals("Remind", menu.label(SearchProgramAction.REMIND, hit, menu.reminderKeys.value, emptySet()))
        }

    @Test
    fun `add to my list toggles the saved programme and the label flips`() =
        runTest {
            val menu = menu()
            assertEquals(
                MyListKeys.ADD_LABEL,
                menu.label(SearchProgramAction.ADD_TO_MY_LIST, hit, emptySet(), menu.myListKeys.value),
            )

            menu.onAction(SearchProgramAction.ADD_TO_MY_LIST, hit)

            assertEquals(SearchOverlay.None, overlays.current.value)
            assertEquals(
                MyListKeys.REMOVE_LABEL,
                menu.label(SearchProgramAction.ADD_TO_MY_LIST, hit, emptySet(), menu.myListKeys.value),
            )

            menu.onAction(SearchProgramAction.ADD_TO_MY_LIST, hit)
            assertTrue(menu.myListKeys.value.isEmpty())
        }

    @Test
    fun `record schedules the airing row's exact slot and closes the dropdown`() =
        runTest {
            menu().onAction(SearchProgramAction.RECORD, hit)

            val row = recordingDao.rows.value.single()
            assertEquals(5_000L, row.startMs)
            assertEquals(6_000L, row.plannedEndMs)
            assertEquals("Politics Tonight", row.title)
            assertEquals(RecordingStatus.SCHEDULED, row.recordingStatus)
            assertEquals(SearchOverlay.None, overlays.current.value)
        }

    @Test
    fun `record on an HLS channel shows the honest explainer instead`() =
        runTest {
            val hls = channel.copy(source = channel.source.copy(streamUrl = "http://h/x.m3u8"))

            menu().onAction(SearchProgramAction.RECORD, hit.copy(channel = hls))

            assertEquals(
                SearchOverlay.Message("Record", RecordingSupport.HLS_MESSAGE),
                overlays.current.value,
            )
        }

    @Test
    fun `custom recording opens the DVR form overlay and BACK drops the form`() =
        runTest {
            val menu = menu()

            menu.onAction(SearchProgramAction.CUSTOM_RECORDING, hit)

            assertEquals(SearchOverlay.CustomRecording, overlays.current.value)
            assertTrue(menu.recording?.form?.value != null)

            assertTrue(menu.closeOverlay())
            assertEquals(SearchOverlay.None, overlays.current.value)
            assertTrue(menu.recording?.form?.value == null)
        }

    @Test
    fun `program description stays on the branded placeholder like the guide`() =
        runTest {
            menu().onAction(SearchProgramAction.PROGRAM_DESCRIPTION, hit)

            assertEquals(SearchOverlay.ComingSoon("Program description"), overlays.current.value)
        }

    @Test
    fun `unwired collaborators fall back to coming-soon like the guide`() =
        runTest {
            val menu = menu(SearchHooks())

            menu.onAction(SearchProgramAction.REMIND, hit)
            assertEquals(SearchOverlay.ComingSoon("Remind"), overlays.current.value)

            menu.onAction(SearchProgramAction.RECORD, hit)
            assertEquals(SearchOverlay.ComingSoon("Record"), overlays.current.value)

            menu.onAction(SearchProgramAction.ADD_TO_MY_LIST, hit)
            assertEquals(SearchOverlay.ComingSoon("Add to My list"), overlays.current.value)

            assertEquals("Remind", menu.label(SearchProgramAction.REMIND, hit, emptySet(), emptySet()))
        }
}
