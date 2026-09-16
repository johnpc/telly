package com.johncorser.telly.features.recording

import com.johncorser.telly.testutil.testChannel
import org.junit.Assert.assertEquals
import org.junit.Test

class CustomRecordingFormTest {
    private val channel = testChannel(1, 1, "News One")

    @Test
    fun `the start defaults to the NEXT five-minute boundary, never now`() {
        // 12:03 -> 12:05, and an exact boundary still moves forward.
        assertEquals(5 * MINUTE, CustomRecordingForm(channel, 3 * MINUTE).startMs.value)
        assertEquals(10 * MINUTE, CustomRecordingForm(channel, 5 * MINUTE).startMs.value)
    }

    @Test
    fun `a boundary only seconds away is skipped -- the default keeps a minute of lead`() {
        // 12:04:30 -> the 12:05 boundary is 30 s out and would already have
        // PASSED by the time Create lands (the acceptance run's 23:59:55
        // form started recording instantly instead of scheduling): the
        // default hops to 12:10.
        assertEquals(10 * MINUTE, CustomRecordingForm(channel, 4 * MINUTE + 30_000L).startMs.value)
        // 59 s of lead is still too little; a full minute is enough.
        assertEquals(10 * MINUTE, CustomRecordingForm(channel, 4 * MINUTE + 1_000L).startMs.value)
        assertEquals(5 * MINUTE, CustomRecordingForm(channel, 4 * MINUTE).startMs.value)
    }

    @Test
    fun `start steps five minutes and never drops below the opening boundary`() {
        val form = CustomRecordingForm(channel, 3 * MINUTE)

        form.adjustStart(+2)
        assertEquals(15 * MINUTE, form.startMs.value)

        form.adjustStart(-10)
        assertEquals(5 * MINUTE, form.startMs.value)
    }

    @Test
    fun `duration steps fifteen minutes within a quarter hour and six hours`() {
        val form = CustomRecordingForm(channel, 0L)
        assertEquals(60, form.durationMinutes.value)

        form.adjustDuration(+1)
        assertEquals(75, form.durationMinutes.value)

        form.adjustDuration(-100)
        assertEquals(15, form.durationMinutes.value)

        form.adjustDuration(+100)
        assertEquals(360, form.durationMinutes.value)
    }

    @Test
    fun `the planned end derives from start plus duration`() {
        val form = CustomRecordingForm(channel, 3 * MINUTE)

        form.adjustDuration(-3)

        assertEquals(form.startMs.value + 15 * MINUTE, form.endMs)
    }

    private companion object {
        const val MINUTE = 60_000L
    }
}
