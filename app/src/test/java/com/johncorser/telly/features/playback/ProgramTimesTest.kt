package com.johncorser.telly.features.playback

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.GregorianCalendar
import java.util.TimeZone

class ProgramTimesTest {
    private val utc = TimeZone.getTimeZone("UTC")

    private fun at(
        hour: Int,
        minute: Int,
        second: Int = 0,
    ): Long =
        GregorianCalendar(utc)
            .apply {
                set(2026, 8, 13, hour, minute, second)
                set(GregorianCalendar.MILLISECOND, 0)
            }.timeInMillis

    @Test
    fun `range shares the meridiem when both ends match`() {
        assertEquals("02:30 — 03:45 PM", ProgramTimes.range(at(14, 30), at(15, 45), utc))
    }

    @Test
    fun `range spells out both meridiems when they differ`() {
        assertEquals("11:30 AM — 12:45 PM", ProgramTimes.range(at(11, 30), at(12, 45), utc))
    }

    @Test
    fun `clock renders the TiviMate header format`() {
        assertEquals("Sun, Sep 13, 2:45 PM", ProgramTimes.clock(at(14, 45), utc))
    }

    @Test
    fun `remaining minutes round up like the capture's 61 min`() {
        assertEquals(61, ProgramTimes.remainingMinutes(at(15, 45), at(14, 44, 30)))
        assertEquals(60, ProgramTimes.remainingMinutes(at(15, 45), at(14, 45)))
        assertEquals(0, ProgramTimes.remainingMinutes(at(15, 45), at(16, 0)))
    }

    @Test
    fun `progress is clamped permille`() {
        assertEquals(500, ProgramTimes.progressPermille(0, 1000, 500))
        assertEquals(0, ProgramTimes.progressPermille(0, 1000, -50))
        assertEquals(1000, ProgramTimes.progressPermille(0, 1000, 2000))
        assertEquals(0, ProgramTimes.progressPermille(1000, 1000, 500))
    }
}
