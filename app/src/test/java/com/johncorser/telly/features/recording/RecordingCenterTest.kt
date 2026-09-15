package com.johncorser.telly.features.recording

import com.johncorser.telly.testutil.testChannel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecordingCenterTest {
    private val dao = FakeRecordingDao()
    private var now = 1_000L
    private val store = RecordingStore(dao) { now }
    private val filesPair = tempRecordingFiles()
    private val files = filesPair.second
    private val hls =
        testChannel(9, 9, "HLS Channel").copy(
            source = testChannel(9, 9, "HLS Channel").source.copy(streamUrl = "http://h/live.m3u8"),
        )
    private val channel = testChannel(1, 1, "News One")

    private fun TestScope.build(
        airing: RecordingProgramme? = null,
        recorder: StreamRecorder = oneShotRecorder(),
    ): RecordingCenter {
        val engine = RecordingEngine(store, recorder, files, this, { now })
        val scheduler = RecordingScheduler(store, engine, { now }, this, MutableSharedFlow())
        return RecordingCenter(store, engine, scheduler, files, { _, _ -> airing }, { now })
    }

    @Test
    fun `instant record runs to the airing programme end when the EPG knows it`() =
        runTest(StandardTestDispatcher()) {
            val center = build(airing = RecordingProgramme("Morning Report", endMs = 4_000L))

            val prompt = center.toggleInstant(channel)
            advanceUntilIdle()

            assertEquals(RecordingPrompt.Done, prompt)
            val row = dao.rows.value.single()
            assertEquals("Morning Report", row.title)
            assertEquals(4_000L, row.plannedEndMs)
        }

    @Test
    fun `instant record without EPG uses the three-hour fallback`() =
        runTest(StandardTestDispatcher()) {
            val center = build(airing = null)

            center.toggleInstant(channel)
            advanceUntilIdle()

            val row = dao.rows.value.single()
            assertEquals("News One", row.title)
            assertEquals(now + RecordingSupport.FALLBACK_DURATION_MS, row.plannedEndMs)
        }

    @Test
    fun `a second record on a recording channel offers Stop`() =
        runTest(StandardTestDispatcher()) {
            // A recorder that parks on its first copy keeps the capture
            // RECORDING; later attempts write nothing so it can wind down.
            val gate = CompletableDeferred<Unit>()
            var parked = false
            val center =
                build(
                    recorder = { _, sink, _ ->
                        if (!parked) {
                            parked = true
                            sink.writeText("x")
                            gate.await()
                            1L
                        } else {
                            0L
                        }
                    },
                )
            center.toggleInstant(channel)
            advanceUntilIdle()

            val prompt = center.toggleInstant(channel)

            assertTrue(prompt is RecordingPrompt.StopConfirm)
            assertEquals("News One", (prompt as RecordingPrompt.StopConfirm).channelName)
            gate.complete(Unit)
        }

    @Test
    fun `HLS channels are reported unsupported instead of recorded`() =
        runTest(StandardTestDispatcher()) {
            val center = build()

            assertEquals(RecordingPrompt.Unsupported("HLS Channel"), center.toggleInstant(hls))
            assertTrue(dao.rows.value.isEmpty())
        }

    @Test
    fun `scheduleProgramme inserts a SCHEDULED slot without starting it`() =
        runTest(StandardTestDispatcher()) {
            val center = build()

            val prompt = center.scheduleProgramme(channel, "Politics Tonight", startMs = 10_000L, endMs = 12_000L)
            advanceUntilIdle()

            assertEquals(RecordingPrompt.Done, prompt)
            val row = dao.rows.value.single()
            assertEquals(RecordingStatus.SCHEDULED, row.recordingStatus)
            assertEquals("Politics Tonight", row.title)
        }

    @Test
    fun `delete drops the row and its capture file`() =
        runTest(StandardTestDispatcher()) {
            val center = build(airing = RecordingProgramme("Morning Report", endMs = 2_000L))
            center.toggleInstant(channel)
            advanceUntilIdle()
            val row = dao.rows.value.single()
            assertTrue(files.exists(row.filePath))

            center.delete(row.id)
            advanceUntilIdle()

            assertTrue(dao.rows.value.isEmpty())
            assertTrue(!files.exists(row.filePath))
        }

    @Test
    fun `deleteAll clears rows and every capture file`() =
        runTest(StandardTestDispatcher()) {
            val center = build(airing = RecordingProgramme("Morning Report", endMs = 2_000L))
            center.toggleInstant(channel)
            advanceUntilIdle()

            center.deleteAll()
            advanceUntilIdle()

            assertEquals(0, center.recordings.first().size)
            assertEquals(0L, center.storage().usedBytes)
        }
}
