package com.johncorser.telly.features.player

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveStreamTest {
    private val active = ActiveStream()

    @Test
    fun `the loaded url is current only while actively streaming`() {
        active.onLoad("http://s/1.ts")

        assertTrue(active.isCurrent("http://s/1.ts", PlayerState.Playing, paused = false))
        assertTrue(active.isCurrent("http://s/1.ts", PlayerState.Buffering, paused = false))
        assertTrue(active.isCurrent("http://s/1.ts", PlayerState.Reconnecting, paused = false))
        assertFalse(active.isCurrent("http://s/2.ts", PlayerState.Playing, paused = false))
    }

    @Test
    fun `an errored, paused or stopped stream is never current`() {
        active.onLoad("http://s/1.ts")

        assertFalse(active.isCurrent("http://s/1.ts", PlayerState.Error("boom"), paused = false))
        assertFalse(active.isCurrent("http://s/1.ts", PlayerState.Playing, paused = true))

        active.onStop()
        assertFalse(active.isCurrent("http://s/1.ts", PlayerState.Playing, paused = false))
    }
}
