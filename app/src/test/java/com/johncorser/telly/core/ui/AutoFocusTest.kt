package com.johncorser.telly.core.ui

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AutoFocusTest {
    @Test
    fun `requests once per frame until the grab lands`() =
        runTest {
            var requests = 0
            var frames = 0
            grabFocusUntilLanded(
                landed = { requests >= 3 },
                request = { requests++ },
                awaitFrame = { frames++ },
            )
            assertEquals(3, requests)
            assertEquals(3, frames)
        }

    @Test
    fun `stops immediately when focus already landed`() =
        runTest {
            var requests = 0
            grabFocusUntilLanded(
                landed = { true },
                request = { requests++ },
                awaitFrame = { },
            )
            assertEquals(0, requests)
        }

    @Test
    fun `gives up after the bounded number of frames`() =
        runTest {
            var requests = 0
            grabFocusUntilLanded(
                landed = { false },
                request = { requests++ },
                awaitFrame = { },
                maxFrames = 5,
            )
            assertEquals(5, requests)
        }

    @Test
    fun `never requests before the node is placed`() =
        runTest {
            var frames = 0
            var requests = 0
            val requestFrames = mutableListOf<Int>()
            grabFocusUntilLanded(
                landed = { requests >= 1 },
                request = {
                    requests++
                    requestFrames += frames
                },
                awaitFrame = { frames++ },
                placed = { frames >= 2 },
            )
            // The grab waited out the two unplaced frames, then requested.
            assertEquals(listOf(2), requestFrames)
        }

    @Test
    fun `a node that never places is never focus-grabbed`() =
        runTest {
            var requests = 0
            var frames = 0
            grabFocusUntilLanded(
                landed = { false },
                request = { requests++ },
                awaitFrame = { frames++ },
                placed = { false },
                maxFrames = 5,
            )
            assertEquals(0, requests)
            assertEquals(5, frames)
        }

    @Test
    fun `a throwing request is swallowed and retried`() =
        runTest {
            var attempts = 0
            grabFocusUntilLanded(
                landed = { attempts >= 2 },
                request = {
                    attempts++
                    error("node not attached yet")
                },
                awaitFrame = { },
            )
            assertEquals(2, attempts)
        }
}
