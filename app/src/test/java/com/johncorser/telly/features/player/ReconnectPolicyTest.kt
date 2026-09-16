package com.johncorser.telly.features.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReconnectPolicyTest {
    @Test
    fun `delays double from the base and cap, then the budget runs out`() {
        val policy = ReconnectPolicy(maxAttempts = 6, baseDelayMs = 1_000L, maxDelayMs = 30_000L)

        val delays = (1..6).map { policy.nextDelayMs() }

        assertEquals(listOf(1_000L, 2_000L, 4_000L, 8_000L, 16_000L, 30_000L), delays)
        assertNull(policy.nextDelayMs())
    }

    @Test
    fun `reset restores the full budget after a successful reconnect`() {
        val policy = ReconnectPolicy(maxAttempts = 2)
        policy.nextDelayMs()
        policy.nextDelayMs()
        assertNull(policy.nextDelayMs())

        policy.reset()

        assertEquals(1_000L, policy.nextDelayMs())
    }

    @Test
    fun `the delay never exceeds the cap`() {
        val policy = ReconnectPolicy(maxAttempts = 10, baseDelayMs = 1_000L, maxDelayMs = 5_000L)

        val delays = (1..10).mapNotNull { policy.nextDelayMs() }

        assertEquals(5_000L, delays.max())
    }
}
