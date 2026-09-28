package com.johncorser.telly.features.player

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StallDetectorTest {
    private val detector = StallDetector(frozenTicks = 3, bufferingTicks = 4)

    @Test
    fun `a frozen position while playing fires after the frozen threshold`() {
        assertFalse(detector.onSample(PlayerState.Playing, true, 42L))
        assertFalse(detector.onSample(PlayerState.Playing, true, 42L))
        assertFalse(detector.onSample(PlayerState.Playing, true, 42L))
        assertTrue(detector.onSample(PlayerState.Playing, true, 42L))
    }

    @Test
    fun `an advancing position never fires`() {
        var position = 0L
        repeat(20) {
            position += 2_000
            assertFalse(detector.onSample(PlayerState.Playing, true, position))
        }
    }

    @Test
    fun `a user pause is never a stall`() {
        repeat(20) {
            assertFalse(detector.onSample(PlayerState.Playing, false, 42L))
        }
    }

    @Test
    fun `movement resets the frozen count`() {
        repeat(3) { detector.onSample(PlayerState.Playing, true, 42L) }
        assertFalse(detector.onSample(PlayerState.Playing, true, 43L))
        repeat(2) { assertFalse(detector.onSample(PlayerState.Playing, true, 43L)) }
        assertTrue(detector.onSample(PlayerState.Playing, true, 43L))
    }

    @Test
    fun `never-ending buffering fires after its own longer threshold`() {
        repeat(3) { assertFalse(detector.onSample(PlayerState.Buffering, true, 0L)) }
        assertTrue(detector.onSample(PlayerState.Buffering, true, 0L))
    }

    @Test
    fun `buffering that resolves to playing resets`() {
        repeat(3) { detector.onSample(PlayerState.Buffering, true, 0L) }
        assertFalse(detector.onSample(PlayerState.Playing, true, 1_000L))
        repeat(3) { assertFalse(detector.onSample(PlayerState.Buffering, true, 1_000L)) }
        assertTrue(detector.onSample(PlayerState.Buffering, true, 1_000L))
    }

    @Test
    fun `idle ended error and reconnecting states never fire`() {
        repeat(20) {
            assertFalse(detector.onSample(PlayerState.Idle, true, 0L))
            assertFalse(detector.onSample(PlayerState.Ended, true, 0L))
            assertFalse(detector.onSample(PlayerState.Error("x"), true, 0L))
            assertFalse(detector.onSample(PlayerState.Reconnecting, true, 0L))
        }
    }

    @Test
    fun `after firing the count restarts instead of firing every tick`() {
        repeat(3) { detector.onSample(PlayerState.Playing, true, 42L) }
        assertTrue(detector.onSample(PlayerState.Playing, true, 42L))
        repeat(2) { assertFalse(detector.onSample(PlayerState.Playing, true, 42L)) }
        assertTrue(detector.onSample(PlayerState.Playing, true, 42L))
    }
}
