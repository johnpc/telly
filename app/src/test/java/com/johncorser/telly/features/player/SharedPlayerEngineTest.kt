package com.johncorser.telly.features.player

import com.johncorser.telly.testutil.FakePlayerEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class SharedPlayerEngineTest {
    private var builds = 0
    private val shared =
        SharedPlayerEngine {
            builds += 1
            FakePlayerEngine()
        }

    @Test
    fun `overlapping leases share one engine build`() {
        val first = shared.acquire()
        val second = shared.acquire()

        assertSame(first, second)
        assertEquals(1, builds)
    }

    @Test
    fun `the crossfade hand-over keeps the engine alive`() {
        // Route crossfade: the incoming screen acquires BEFORE the outgoing
        // screen disposes, so the stream must survive the outgoing release.
        val outgoing = shared.acquire()
        shared.acquire()
        shared.release()

        assertFalse(outgoing.released)
    }

    @Test
    fun `the last release frees the engine and the next acquire rebuilds`() {
        val first = shared.acquire()
        shared.release()

        assertTrue(first.released)
        assertNotSame(first, shared.acquire())
        assertEquals(2, builds)
    }

    @Test
    fun `an unbalanced release never underflows the lease count`() {
        shared.release()
        val engine = shared.acquire()
        shared.release()

        assertTrue(engine.released)
    }
}
