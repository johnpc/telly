package com.johncorser.telly.features.playback

import com.johncorser.telly.features.recording.FakeRecordingDao
import com.johncorser.telly.features.recording.RecordingCenter
import com.johncorser.telly.features.recording.RecordingEngine
import com.johncorser.telly.features.recording.RecordingProgramme
import com.johncorser.telly.features.recording.RecordingScheduler
import com.johncorser.telly.features.recording.RecordingStatus
import com.johncorser.telly.features.recording.RecordingStore
import com.johncorser.telly.features.recording.oneShotRecorder
import com.johncorser.telly.features.recording.tempRecordingFiles
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The transport record dot: state derivation + the instant-record toggle. */
@OptIn(ExperimentalCoroutinesApi::class)
class PlaybackRecordTest : PlaybackVmHarness() {
    private val recordingDao = FakeRecordingDao()

    private fun TestScope.center(): RecordingCenter {
        val recordingStore = RecordingStore(recordingDao) { now }
        val files = tempRecordingFiles().second
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val recordingEngine = RecordingEngine(recordingStore, oneShotRecorder(), files, scope, { now })
        val scheduler = RecordingScheduler(recordingStore, recordingEngine, { now }, scope, MutableSharedFlow())
        val airing = { _: String, _: Long -> RecordingProgramme("Morning Report", endMs = now + 3_600_000L) }
        return RecordingCenter(recordingStore, recordingEngine, scheduler, files, airing, { now })
    }

    @Test
    fun `without a DVR the dot stays grey and the toggle lands on coming-soon`() =
        runTest {
            val vm = buildVm()

            assertFalse(vm.record.active.value)
            vm.record.toggle()

            assertEquals(PlaybackOverlay.ComingSoon("Record"), vm.overlay.value)
        }

    @Test
    fun `the dot turns red while the tuned channel records and toggling offers Stop`() =
        runTest {
            val vm = buildVm(hooks = PlaybackHooks(recording = center()))
            assertFalse(vm.record.active.value)

            vm.record.toggle()

            assertTrue(vm.record.active.value)
            assertEquals(RecordingStatus.RECORDING.name, recordingDao.rows.value.single().status)
            assertEquals(PlaybackOverlay.None, vm.overlay.value)

            // A second press is the sheet's Stop path, not a silent restart.
            vm.record.toggle()
            val overlay = vm.overlay.value as PlaybackOverlay.RecordingStop
            assertEquals("News One", overlay.channelName)

            vm.record.menu?.confirmStop(overlay.recordingId)
            advanceUntilIdle()
            assertFalse(vm.record.active.value)
            assertEquals(RecordingStatus.DONE.name, recordingDao.rows.value.single().status)
        }

    @Test
    fun `zapping to another channel drops the red state`() =
        runTest {
            val vm = buildVm(hooks = PlaybackHooks(recording = center()))
            vm.record.toggle()
            assertTrue(vm.record.active.value)

            vm.onKey(PlaybackKey.CHANNEL_UP)

            assertFalse(vm.record.active.value)
        }
}
