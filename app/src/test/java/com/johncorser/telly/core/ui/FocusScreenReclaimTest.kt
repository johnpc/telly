package com.johncorser.telly.core.ui

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FocusScreenReclaimTest {
    @Test
    fun `reclaim advances the epoch`() {
        val reclaim = FocusScreenReclaim()
        reclaim.reclaim()
        reclaim.reclaim()
        assertEquals(2, reclaim.epoch.value)
    }

    @Test
    fun `epoch zero is a no-op - nothing was reclaimed yet`() =
        runTest {
            var frames = 0
            FocusScreenReclaim().grab(0) { frames++ }
            assertEquals(0, frames)
        }

    @Test
    fun `a grab waits for placement then retries until landed`() =
        runTest {
            val reclaim = FocusScreenReclaim()
            var frames = 0
            reclaim.reclaim()
            // The detached requester throws on every request; the engine
            // swallows and retries, so landing is what ends the loop.
            reclaim.grab(reclaim.epoch.value) {
                frames++
                if (frames == 2) reclaim.nodePlaced(true)
                if (frames == 4) reclaim.nodeFocused(true)
            }
            assertEquals(4, frames)
        }

    @Test
    fun `a re-grab clears the previous landing`() =
        runTest {
            val reclaim = FocusScreenReclaim()
            reclaim.nodePlaced(true)
            reclaim.nodeFocused(true)
            var frames = 0
            reclaim.reclaim()
            reclaim.grab(reclaim.epoch.value) {
                frames++
                if (frames == 3) reclaim.nodeFocused(true)
            }
            assertEquals(3, frames)
        }

    @Test
    fun `a target that never places bounds the grab`() =
        runTest {
            val reclaim = FocusScreenReclaim()
            var frames = 0
            reclaim.reclaim()
            reclaim.grab(reclaim.epoch.value) { frames++ }
            assertEquals(30, frames)
        }

    @Test
    fun `losing focus never counts as landed`() =
        runTest {
            val reclaim = FocusScreenReclaim()
            reclaim.nodePlaced(true)
            var frames = 0
            reclaim.reclaim()
            reclaim.grab(reclaim.epoch.value) {
                frames++
                reclaim.nodeFocused(false)
            }
            assertEquals(30, frames)
        }
}
