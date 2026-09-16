package com.johncorser.telly.features.recording

import com.johncorser.telly.testutil.testChannel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecordingMenuTest {
    private val dao = FakeRecordingDao()
    private var now = 1_000L
    private val store = RecordingStore(dao) { now }
    private val files = tempRecordingFiles().second
    private val channel = testChannel(1, 1, "News One")
    private val prompts = mutableListOf<RecordingPrompt>()

    private fun TestScope.menu(airing: RecordingProgramme? = null): RecordingMenu {
        val engine = RecordingEngine(store, oneShotRecorder(), files, this, { now })
        val scheduler = RecordingScheduler(store, engine, { now }, this, MutableSharedFlow())
        val center = RecordingCenter(store, engine, scheduler, files, { _, _ -> airing }, { now })
        return RecordingMenu(center, this, { now }) { prompts += it }
    }

    @Test
    fun `onRecord shows Done and creates the capture`() =
        runTest(StandardTestDispatcher()) {
            menu(airing = RecordingProgramme("Morning Report", endMs = 2_000L)).onRecord(channel)
            advanceUntilIdle()

            assertEquals(listOf(RecordingPrompt.Done), prompts)
            assertEquals(1, dao.rows.value.size)
        }

    @Test
    fun `onCustomRecording opens the form then Create schedules and closes`() =
        runTest(StandardTestDispatcher()) {
            val menu = menu()

            menu.onCustomRecording(channel)
            assertEquals(RecordingPrompt.CustomForm, prompts.last())
            assertTrue(menu.form.value != null)

            menu.createFromForm()
            advanceUntilIdle()

            assertEquals(RecordingPrompt.Done, prompts.last())
            assertNull(menu.form.value)
            assertEquals(RecordingStatus.SCHEDULED, dao.rows.value.single().recordingStatus)
        }

    @Test
    fun `onCustomRecording on an HLS channel opens the form like any other`() =
        runTest(StandardTestDispatcher()) {
            val hls = channel.copy(source = channel.source.copy(streamUrl = "http://h/x.m3u8"))
            val menu = menu()

            menu.onCustomRecording(hls)

            assertEquals(RecordingPrompt.CustomForm, prompts.last())
            assertEquals(hls, menu.form.value?.channel)
        }

    @Test
    fun `onRecordProgramme schedules the given slot`() =
        runTest(StandardTestDispatcher()) {
            menu().onRecordProgramme(channel, "Politics Tonight", startMs = 5_000L, endMs = 6_000L)
            advanceUntilIdle()

            val row = dao.rows.value.single()
            assertEquals("Politics Tonight", row.title)
            assertEquals(5_000L, row.startMs)
        }

    @Test
    fun `dismissForm drops the open form`() =
        runTest(StandardTestDispatcher()) {
            val menu = menu()
            menu.onCustomRecording(channel)

            menu.dismissForm()

            assertNull(menu.form.value)
        }
}
