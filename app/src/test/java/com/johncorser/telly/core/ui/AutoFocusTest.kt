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
