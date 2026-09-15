package com.johncorser.telly.features.recording

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RecordingStoreTest {
    private val dao = FakeRecordingDao()
    private var now = 5_000_000L
    private val store = RecordingStore(dao) { now }

    @Test
    fun `schedule inserts a SCHEDULED row and hands back its id`() =
        runTest {
            val entry = store.schedule(recordingEntity(status = RecordingStatus.RECORDING))

            assertNotNull(entry)
            assertEquals(1L, entry!!.id)
            assertEquals(RecordingStatus.SCHEDULED, entry.recordingStatus)
            assertEquals(RecordingStatus.SCHEDULED.name, dao.rows.value.single().status)
        }

    @Test
    fun `scheduling the same channel slot twice is ignored`() =
        runTest {
            store.schedule(recordingEntity())
            val duplicate = store.schedule(recordingEntity())

            assertNull(duplicate)
            assertEquals(1, dao.rows.value.size)
        }

    @Test
    fun `a finished slot can be recorded again`() =
        runTest {
            val first = store.schedule(recordingEntity())!!
            store.complete(first.id, sizeBytes = 10)

            assertNotNull(store.schedule(recordingEntity()))
        }

    @Test
    fun `markStarted stamps the injected clock as the actual start`() =
        runTest {
            val entry = store.schedule(recordingEntity(startMs = 1_000L))!!

            now = 7_777L
            store.markStarted(entry.id)

            val row = dao.rows.value.single()
            assertEquals(RecordingStatus.RECORDING, row.recordingStatus)
            assertEquals(7_777L, row.startMs)
        }

    @Test
    fun `complete and fail stamp the end clock and the final size`() =
        runTest {
            val first = store.schedule(recordingEntity())!!
            val second = store.schedule(recordingEntity(channelKey = "tvg-2"))!!

            now = 9_000L
            store.complete(first.id, sizeBytes = 123)
            store.fail(second.id)

            val rows = dao.rows.value.associateBy { it.id }
            assertEquals(RecordingStatus.DONE, rows.getValue(first.id).recordingStatus)
            assertEquals(9_000L, rows.getValue(first.id).endMs)
            assertEquals(123L, rows.getValue(first.id).sizeBytes)
            assertEquals(RecordingStatus.FAILED, rows.getValue(second.id).recordingStatus)
        }

    @Test
    fun `activeFor finds only the in-progress capture of that channel`() =
        runTest {
            val entry = store.schedule(recordingEntity())!!
            assertNull(store.activeFor("tvg-1"))

            store.markStarted(entry.id)

            assertEquals(entry.id, store.activeFor("tvg-1")?.id)
            assertNull(store.activeFor("tvg-2"))
            assertEquals(listOf(entry.id), store.allRecording().map { it.id })
        }

    @Test
    fun `delete and deleteAll drop rows`() =
        runTest {
            val first = store.schedule(recordingEntity())!!
            store.schedule(recordingEntity(channelKey = "tvg-2"))

            store.delete(first.id)
            assertEquals(1, store.recordings.first().size)

            store.deleteAll()
            assertEquals(0, store.recordings.first().size)
        }
}
