package com.johncorser.telly.features.recording

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class RecordingEngineTest {
    private val dao = FakeRecordingDao()
    private var now = 1_000L
    private val store = RecordingStore(dao) { now }
    private val filesPair = tempRecordingFiles()
    private val files = filesPair.second
    private val service = FakeServiceControl()

    private fun TestScope.engine(recorder: StreamRecorder): RecordingEngine =
        RecordingEngine(store, recorder, files, this, { now }, service)

    private fun captureFile(name: String): File = files.newFile(name, 0L)

    @Test
    fun `a capture writes bytes, ends DONE at the planned end and drops the service`() =
        runTest(StandardTestDispatcher()) {
            val file = captureFile("News One")
            val entry =
                store.schedule(recordingEntity(filePath = file.path, plannedEndMs = 5_000L))!!
            val recorder =
                StreamRecorder { _, sink, _ ->
                    sink.writeText("tsdata")
                    now = 6_000L // past the planned end -> the loop ends
                    6L
                }

            engine(recorder).start(entry)
            advanceUntilIdle()

            val row = dao.rows.value.single()
            assertEquals(RecordingStatus.DONE, row.recordingStatus)
            assertEquals(6L, row.sizeBytes)
            assertTrue(service.synced.last().isEmpty())
        }

    @Test
    fun `a capture that never writes fails after the idle attempts`() =
        runTest(StandardTestDispatcher()) {
            val file = captureFile("News One")
            val entry = store.schedule(recordingEntity(filePath = file.path, plannedEndMs = Long.MAX_VALUE))!!
            var attempts = 0
            val recorder =
                StreamRecorder { _, _, _ ->
                    attempts++
                    throw IOException("down")
                }

            engine(recorder).start(entry)
            advanceUntilIdle()

            assertEquals(RecordingStatus.FAILED, dao.rows.value.single().recordingStatus)
            assertEquals(3, attempts)
        }

    @Test
    fun `stop ends the loop and the row lands DONE with what was written`() =
        runTest(StandardTestDispatcher()) {
            val file = captureFile("News One").apply { writeText("live") }
            val entry = store.schedule(recordingEntity(filePath = file.path, plannedEndMs = Long.MAX_VALUE))!!
            // The recorder writes nothing further and honours shouldStop.
            val recorder = StreamRecorder { _, _, _ -> 0L }
            val engine = engine(recorder)

            engine.start(entry)
            assertTrue(engine.isActive(entry.id))
            engine.stop(entry.id) // sets stopped, then joins the copy loop
            advanceUntilIdle()

            assertEquals(RecordingStatus.DONE, dao.rows.value.single().recordingStatus)
            assertFalse(engine.isActive(entry.id))
        }

    @Test
    fun `recoverStale finalizes rows left RECORDING by a killed process`() =
        runTest(StandardTestDispatcher()) {
            val file = captureFile("News One").apply { writeText("partial") }
            val entry = store.schedule(recordingEntity(filePath = file.path))!!
            store.markStarted(entry.id)

            engine(StreamRecorder { _, _, _ -> 0L }).recoverStale()
            advanceUntilIdle()

            assertEquals(RecordingStatus.DONE, dao.rows.value.single().recordingStatus)
        }
}
