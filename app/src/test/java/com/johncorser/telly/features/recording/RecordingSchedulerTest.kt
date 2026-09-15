package com.johncorser.telly.features.recording

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecordingSchedulerTest {
    private val dao = FakeRecordingDao()
    private var now = 1_000L
    private val store = RecordingStore(dao) { now }
    private val files = tempRecordingFiles().second
    private val ticks = MutableSharedFlow<Unit>()

    // A dedicated eager scope: the scheduler's collector and the capture
    // coroutines run inline as flows emit, and it is not a child of the
    // test scope, so runTest never flags it as a leak.
    private fun TestScope.eagerScope() = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))

    private fun startedScheduler(scope: CoroutineScope) {
        val engine = RecordingEngine(store, idleRecorder(), files, scope, { now })
        RecordingScheduler(store, engine, { now }, scope, ticks).start()
    }

    @Test
    fun `a due scheduled recording starts on the sweep`() =
        runTest(UnconfinedTestDispatcher()) {
            startedScheduler(eagerScope())
            store.schedule(
                recordingEntity(startMs = 500L, plannedEndMs = 5_000L, filePath = files.newFile("A", 0L).path),
            )

            // now (1000) >= startMs (500) -> the sweep starts it (RECORDING), not SCHEDULED
            assertEquals(0, dao.byStatus(RecordingStatus.SCHEDULED.name).size)
        }

    @Test
    fun `a future recording waits until its start time ticks by`() =
        runTest(UnconfinedTestDispatcher()) {
            startedScheduler(eagerScope())
            store.schedule(recordingEntity(startMs = 10_000L, plannedEndMs = 20_000L))
            assertEquals(1, dao.byStatus(RecordingStatus.SCHEDULED.name).size)

            now = 10_000L
            ticks.emit(Unit)

            assertEquals(0, dao.byStatus(RecordingStatus.SCHEDULED.name).size)
        }

    @Test
    fun `a recording whose whole window was missed is failed, not started`() =
        runTest(UnconfinedTestDispatcher()) {
            startedScheduler(eagerScope())
            now = 9_999L
            store.schedule(recordingEntity(startMs = 1_000L, plannedEndMs = 2_000L))

            assertEquals(RecordingStatus.FAILED, dao.rows.value.single().recordingStatus)
        }
}
