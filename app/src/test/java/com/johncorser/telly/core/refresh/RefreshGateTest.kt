package com.johncorser.telly.core.refresh

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RefreshGateTest {
    private val lines = mutableListOf<String>()

    @Test
    fun `a run returns its result and logs only when it did real work`() =
        runTest {
            var now = 1_000L
            val gate = RefreshGate(clock = { now }, log = { lines += it })

            val quick =
                gate.run("EPG refresh", onTimeout = -1) {
                    now += 20
                    1
                }
            val slow =
                gate.run("EPG refresh", onTimeout = -1) {
                    now += 6_500
                    42
                }

            assertEquals(1, quick)
            assertEquals(42, slow)
            assertEquals("the 20 ms nothing-due check stays quiet", listOf("EPG refresh: done in 6500ms"), lines)
        }

    @Test
    fun `a second refresh waits for the first one to finish`() =
        runTest {
            val gate = RefreshGate(log = { lines += it })
            val release = CompletableDeferred<Unit>()
            val order = mutableListOf<String>()

            launch {
                gate.run("EPG refresh", Unit) {
                    order += "epg start"
                    release.await()
                    order += "epg end"
                }
            }
            launch { gate.run("Playlist refresh", Unit) { order += "playlist" } }
            runCurrent()
            assertEquals("the playlist import is queued behind the EPG write", listOf("epg start"), order)

            release.complete(Unit)
            runCurrent()
            assertEquals(listOf("epg start", "epg end", "playlist"), order)
        }

    @Test
    fun `a wedged run is abandoned after the timeout and the next run proceeds`() =
        runTest {
            val gate = RefreshGate(log = { lines += it }, timeoutMs = 1_000L)
            var after = false

            launch {
                assertEquals(
                    emptyList<Long>(),
                    gate.run("EPG refresh", emptyList<Long>()) { awaitCancellation() },
                )
            }
            launch { gate.run("Playlist refresh", Unit) { after = true } }
            advanceTimeBy(1_001L)
            runCurrent()

            assertTrue("the queued refresh ran once the wedged one was abandoned", after)
            assertTrue(lines.any { it.startsWith("EPG refresh: TIMED OUT") })
        }
}
