package com.johncorser.telly.features.multiview

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class MultiviewPaneFocusTest {
    @Test
    fun `never requests before the pane is placed`() =
        runTest {
            val tracker = MultiviewPaneFocus()
            var requests = 0
            var frames = 0
            tracker.grabOnto(
                id = 1,
                request = { requests++ },
                awaitFrame = {
                    frames++
                    if (frames == 2) tracker.onPanePlaced(1)
                    if (frames == 4) tracker.onPaneFocusChanged(1, true)
                },
            )
            // Two unplaced frames pass, then one request per frame until
            // the pane reports focus.
            assertEquals(2, requests)
        }

    @Test
    fun `an already-focused pane is left alone`() =
        runTest {
            val tracker = MultiviewPaneFocus()
            tracker.onPanePlaced(3)
            tracker.onPaneFocusChanged(3, true)
            var requests = 0
            tracker.grabOnto(id = 3, request = { requests++ }, awaitFrame = { })
            assertEquals(0, requests)
        }

    @Test
    fun `losing focus to another pane moves the owner`() =
        runTest {
            val tracker = MultiviewPaneFocus()
            tracker.onPanePlaced(1)
            tracker.onPanePlaced(2)
            tracker.onPaneFocusChanged(1, true)
            tracker.onPaneFocusChanged(2, true)
            tracker.onPaneFocusChanged(1, false)
            var requests = 0
            tracker.grabOnto(id = 2, request = { requests++ }, awaitFrame = { })
            assertEquals(0, requests)
        }

    @Test
    fun `pruning a removed pane re-arms its placement gate`() =
        runTest {
            val tracker = MultiviewPaneFocus()
            tracker.onPanePlaced(1)
            tracker.onPanePlaced(2)
            tracker.prune(listOf(2))
            var requests = 0
            var frames = 0
            tracker.grabOnto(
                id = 1,
                request = { requests++ },
                awaitFrame = { frames++ },
            )
            // Pane 1 left the grid: no placement, no request, bounded.
            assertEquals(0, requests)
            assertEquals(30, frames)
        }
}
