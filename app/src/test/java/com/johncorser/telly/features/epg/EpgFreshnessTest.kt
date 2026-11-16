package com.johncorser.telly.features.epg

import com.johncorser.telly.features.playlist.db.PlaylistEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EpgFreshnessTest {
    private val hour = RefreshScheduler.HOUR_MS
    private val daily = RefreshScheduler(RefreshScheduler.DEFAULT_INTERVAL_MS)
    private val never = RefreshScheduler { RefreshScheduler.NEVER_MS }
    private val backoff = EpgFreshness.RETRY_BACKOFF_MS

    private fun playlist(
        updated: Long = 0,
        failed: Long = 0,
    ) = PlaylistEntity(id = 1, name = "p", url = "http://p", epgLastUpdatedMs = updated, epgFailedAtMs = failed)

    @Test
    fun `the two stamps classify into one freshness state`() {
        assertEquals(EpgFreshness.NeverFetched, EpgFreshness.of(playlist()))
        assertEquals(EpgFreshness.Fresh(5 * hour), EpgFreshness.of(playlist(updated = 5 * hour)))
        assertEquals(EpgFreshness.Failed(6 * hour, 0), EpgFreshness.of(playlist(failed = 6 * hour)))
        // A failure after a success keeps the success for the interval math.
        val failedAfterSuccess = EpgFreshness.of(playlist(updated = 5 * hour, failed = 7 * hour))
        assertEquals(EpgFreshness.Failed(7 * hour, 5 * hour), failedAfterSuccess)
        // A success after a failure is simply fresh.
        assertEquals(EpgFreshness.Fresh(8 * hour), EpgFreshness.of(playlist(updated = 8 * hour, failed = 7 * hour)))
    }

    @Test
    fun `never fetched is always due, fresh follows the interval`() {
        assertTrue(EpgFreshness.isDue(EpgFreshness.NeverFetched, 0, never))
        assertFalse(EpgFreshness.isDue(EpgFreshness.Fresh(hour), 24 * hour, daily))
        assertTrue(EpgFreshness.isDue(EpgFreshness.Fresh(hour), 25 * hour, daily))
        // Interval "None" never re-fetches data that was fetched once.
        assertFalse(EpgFreshness.isDue(EpgFreshness.Fresh(hour), 240 * hour, never))
    }

    @Test
    fun `a failure backs off and then honors the interval against the last success`() {
        val failed = EpgFreshness.Failed(failedAtMs = 10 * hour, lastSuccessMs = 0)
        assertFalse(EpgFreshness.isDue(failed, 10 * hour + backoff - 1, never))
        // Never succeeded: due as soon as the backoff passes.
        assertTrue(EpgFreshness.isDue(failed, 10 * hour + backoff, never))

        val failedAfterSuccess = EpgFreshness.Failed(failedAtMs = 10 * hour, lastSuccessMs = 9 * hour)
        // Backoff passed, but the daily interval since the last success has not.
        assertFalse(EpgFreshness.isDue(failedAfterSuccess, 11 * hour, daily))
        assertTrue(EpgFreshness.isDue(failedAfterSuccess, 33 * hour, daily))
    }
}
