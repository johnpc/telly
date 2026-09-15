package com.johncorser.telly.features.catchup

import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.withCatchup
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CatchupPlayabilityTest {
    private val hourMs = 3_600_000L
    private val dayMs = 24 * hourMs
    private val nowMs = 40 * dayMs
    private val channel = testChannel(1, 1, "News One").withCatchup(days = 2)

    private fun playable(
        startMs: Long,
        endMs: Long,
        hasInfo: Boolean = true,
        target: com.johncorser.telly.features.playlist.db.ChannelEntity = channel,
    ): Boolean = CatchupPlayability.playable(target, startMs, endMs, hasInfo, nowMs)

    @Test
    fun `a fully aired programme within the horizon is playable`() {
        assertTrue(playable(nowMs - 3 * hourMs, nowMs - 2 * hourMs))
    }

    @Test
    fun `airing and future programmes are never catch-up`() {
        assertFalse(playable(nowMs - hourMs, nowMs + hourMs))
        assertFalse(playable(nowMs + hourMs, nowMs + 2 * hourMs))
    }

    @Test
    fun `programmes older than catchup-days fall outside the horizon`() {
        assertFalse(playable(nowMs - 2 * dayMs - hourMs, nowMs - 2 * dayMs))
        assertTrue(playable(nowMs - 2 * dayMs, nowMs - 2 * dayMs + hourMs))
    }

    @Test
    fun `no-information filler cells are not playable`() {
        assertFalse(playable(nowMs - 3 * hourMs, nowMs - 2 * hourMs, hasInfo = false))
    }

    @Test
    fun `channels without catch-up keep today's behavior`() {
        assertFalse(playable(nowMs - 3 * hourMs, nowMs - 2 * hourMs, target = testChannel(2, 2, "Plain")))
    }
}
