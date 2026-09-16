package com.johncorser.telly.features.recording

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class RecordingsViewModelTest {
    private val dao = FakeRecordingDao()
    private var now = 1_000L
    private val store = RecordingStore(dao) { now }
    private val files = tempRecordingFiles().second

    // Eager, non-child scope: stateIn collects inline and runTest never
    // flags it as a leaked child (the repo's ViewModel-test pattern).
    private fun TestScope.eagerScope() = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))

    private fun center(scope: CoroutineScope): RecordingCenter {
        val engine = RecordingEngine(store, { _, _, _ -> 0L }, files, scope, { now })
        val scheduler = RecordingScheduler(store, engine, { now }, scope, MutableSharedFlow())
        return RecordingCenter(store, engine, scheduler, files, { _, _ -> null }, { now })
    }

    private fun TestScope.viewModel(): RecordingsViewModel {
        val scope = eagerScope()
        return RecordingsViewModel(center(scope), TimeZone.getTimeZone("UTC"), scope)
    }

    private suspend fun seed(status: RecordingStatus): Long =
        dao.insert(recordingEntity(status = status, filePath = files.newFile("A", 0L).path))

    @Test
    fun `OK on a DONE row plays it, no confirm`() =
        runTest(UnconfinedTestDispatcher()) {
            seed(RecordingStatus.DONE)
            val vm = viewModel()
            advanceUntilIdle()

            vm.onRowClick(vm.rows.value.single())

            assertNull(vm.confirm.value)
            assertEquals(RecordingStatus.DONE, vm.playing.value?.status)
            vm.stopPlaying()
            assertNull(vm.playing.value)
        }

    @Test
    fun `OK on a scheduled row confirms cancel, on a recording row confirms stop`() =
        runTest(UnconfinedTestDispatcher()) {
            seed(RecordingStatus.SCHEDULED)
            seed(RecordingStatus.RECORDING)
            val vm = viewModel()
            advanceUntilIdle()
            val rows = vm.rows.value.associateBy { it.status }

            vm.onRowClick(rows.getValue(RecordingStatus.SCHEDULED))
            assertTrue(vm.confirm.value is RecordingsConfirm.Cancel)

            vm.onRowClick(rows.getValue(RecordingStatus.RECORDING))
            assertTrue(vm.confirm.value is RecordingsConfirm.Stop)
        }

    @Test
    fun `OK right after confirming Stop plays the capture instead of re-confirming`() =
        runTest(UnconfinedTestDispatcher()) {
            seed(RecordingStatus.RECORDING)
            val vm = viewModel()
            advanceUntilIdle()
            val recording = vm.rows.value.single()
            vm.onRowClick(recording)
            assertTrue(vm.confirm.value is RecordingsConfirm.Stop)

            vm.onConfirmAccepted()
            // The capture winds down asynchronously: the row snapshot still
            // says RECORDING when the user immediately presses OK again —
            // the UI truth is stopped, so OK must play, not re-confirm.
            vm.onRowClick(recording)

            assertNull(vm.confirm.value)
            assertEquals(recording.entry.id, vm.playing.value?.entry?.id)
        }

    @Test
    fun `long-OK on any row confirms delete and accepting removes it`() =
        runTest(UnconfinedTestDispatcher()) {
            seed(RecordingStatus.DONE)
            val vm = viewModel()
            advanceUntilIdle()

            vm.onRowLongClick(vm.rows.value.single())
            assertTrue(vm.confirm.value is RecordingsConfirm.Delete)

            vm.onConfirmAccepted()
            advanceUntilIdle()

            assertTrue(vm.rows.value.isEmpty())
            assertNull(vm.confirm.value)
        }

    @Test
    fun `OK on a FAILED row goes straight to a delete confirm`() =
        runTest(UnconfinedTestDispatcher()) {
            seed(RecordingStatus.FAILED)
            val vm = viewModel()
            advanceUntilIdle()

            vm.onRowClick(vm.rows.value.single())

            assertTrue(vm.confirm.value is RecordingsConfirm.Delete)
        }

    @Test
    fun `dismissing the confirm clears it without acting`() =
        runTest(UnconfinedTestDispatcher()) {
            seed(RecordingStatus.SCHEDULED)
            val vm = viewModel()
            advanceUntilIdle()
            vm.onRowClick(vm.rows.value.single())

            vm.dismissConfirm()

            assertNull(vm.confirm.value)
            assertEquals(1, vm.rows.value.size)
        }
}
