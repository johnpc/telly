package com.johncorser.telly.features.player

import androidx.media3.exoplayer.ExoPlayer
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EngineStallWatchdogTest {
    private val player = mockk<ExoPlayer>(relaxed = true)
    private val scheduled = mutableListOf<Pair<Long, () -> Unit>>()
    private var state: PlayerState = PlayerState.Playing
    private var stalls = 0

    private fun watchdog(detector: StallDetector = StallDetector(frozenTicks = 2, bufferingTicks = 3)) =
        EngineStallWatchdog(
            player = player,
            detector = detector,
            schedule = { delayMs, task -> scheduled += delayMs to task },
            stateOf = { state },
            onStall = { stalls++ },
        )

    /** Runs the next pending tick (each tick reschedules the following one). */
    private fun tick() = scheduled.removeAt(0).second()

    @Test
    fun `samples on the tick cadence and recovers a frozen stream`() {
        every { player.playWhenReady } returns true
        every { player.currentPosition } returns 42L
        watchdog().start()

        assertEquals(EngineStallWatchdog.TICK_MS, scheduled.single().first)
        tick() // baseline sample
        assertEquals(0, stalls)
        tick() // first frozen sample
        assertEquals(0, stalls)
        tick() // second frozen sample: threshold reached
        assertEquals(1, stalls)
        assertTrue(scheduled.isNotEmpty()) // keeps ticking after a recovery
    }

    @Test
    fun `a healthy stream never triggers a recovery`() {
        every { player.playWhenReady } returns true
        var position = 0L
        every { player.currentPosition } answers {
            position += 2_000
            position
        }
        watchdog().start()

        repeat(10) { tick() }

        assertEquals(0, stalls)
    }

    @Test
    fun `stop halts the sampling for good`() {
        every { player.playWhenReady } returns true
        every { player.currentPosition } returns 42L
        val watchdog = watchdog()
        watchdog.start()
        tick()

        watchdog.stop()
        tick()

        assertTrue(scheduled.isEmpty()) // the stopped tick did not reschedule
        assertEquals(0, stalls)
    }
}
