package com.johncorser.telly.features.epg

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RefreshSchedulerTest {
    private val dayMs = 24L * 60 * 60 * 1000

    @Test
    fun `defaults to tivimate's 24 hour interval`() {
        assertEquals(dayMs, RefreshScheduler.DEFAULT_INTERVAL_MS)
    }

    @Test
    fun `never-updated data is always due`() {
        assertTrue(RefreshScheduler().isDue(lastUpdatedMs = 0, nowMs = 1))
        assertTrue(RefreshScheduler().isDue(lastUpdatedMs = -5, nowMs = 1))
    }

    @Test
    fun `fresh data is not due`() {
        val scheduler = RefreshScheduler()
        assertFalse(scheduler.isDue(lastUpdatedMs = 1_000, nowMs = 1_000))
        assertFalse(scheduler.isDue(lastUpdatedMs = 1_000, nowMs = 1_000 + dayMs - 1))
    }

    @Test
    fun `data becomes due exactly one interval after the last update`() {
        val scheduler = RefreshScheduler()
        assertTrue(scheduler.isDue(lastUpdatedMs = 1_000, nowMs = 1_000 + dayMs))
        assertTrue(scheduler.isDue(lastUpdatedMs = 1_000, nowMs = 1_000 + dayMs + 1))
    }

    @Test
    fun `custom intervals are honored`() {
        val hourly = RefreshScheduler(intervalMs = 3_600_000)
        assertFalse(hourly.isDue(lastUpdatedMs = 1, nowMs = 3_600_000))
        assertTrue(hourly.isDue(lastUpdatedMs = 1, nowMs = 3_600_001))
    }

    @Test
    fun `non-positive intervals are rejected`() {
        try {
            RefreshScheduler(intervalMs = 0)
            throw AssertionError("expected IllegalArgumentException")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message!!.contains("intervalMs"))
        }
    }
}
