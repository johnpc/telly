package com.johncorser.telly.features.recording

import com.johncorser.telly.features.recording.db.RecordingEntity
import com.johncorser.telly.testutil.testChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecordingIndicatorTest {
    private val channel = testChannel(1, 1, "News One")
    private val other = testChannel(2, 2, "News Two")

    @Test
    fun `red only while the tuned channel has an in-progress recording`() {
        val recording = listOf(recordingEntity(status = RecordingStatus.RECORDING))

        assertTrue(RecordingIndicator.isRecording(recording, channel))
        assertFalse(RecordingIndicator.isRecording(recording, other))
        assertFalse(RecordingIndicator.isRecording(recording, null))
    }

    @Test
    fun `scheduled and finished entries keep the dot grey`() {
        val entries =
            listOf(
                recordingEntity(status = RecordingStatus.SCHEDULED),
                recordingEntity(status = RecordingStatus.DONE),
                recordingEntity(status = RecordingStatus.FAILED),
            )

        assertFalse(RecordingIndicator.isRecording(entries, channel))
    }

    @Test
    fun `the flow follows the library feed and the tuned channel`() =
        runTest {
            val recordings = MutableStateFlow<List<RecordingEntity>>(emptyList())
            val current = MutableStateFlow(channel)
            val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))

            val active = RecordingIndicator.activeFlow(recordings, current, scope)
            assertFalse(active.value)

            recordings.value = listOf(recordingEntity(status = RecordingStatus.RECORDING))
            assertTrue(active.value)

            current.value = other
            assertFalse(active.value)
        }

    @Test
    fun `a missing DVR feed never turns the dot red`() =
        runTest {
            val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))

            val active = RecordingIndicator.activeFlow(null, MutableStateFlow(channel), scope)

            assertFalse(active.value)
        }
}
