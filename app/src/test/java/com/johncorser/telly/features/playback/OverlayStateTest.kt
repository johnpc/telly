package com.johncorser.telly.features.playback

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OverlayStateTest {
    private fun TestScope.build(): OverlayState =
        OverlayState(CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler)))

    @Test
    fun `each transient overlay hides after its own timeout`() =
        runTest {
            val state = build()

            state.showAutoHiding(PlaybackOverlay.ZapInfo, timeoutMs = 5_500)
            advanceTimeBy(5_499)
            assertEquals(PlaybackOverlay.ZapInfo, state.value)
            advanceTimeBy(2)
            assertEquals(PlaybackOverlay.None, state.value)

            state.showAutoHiding(PlaybackOverlay.QuickBar, timeoutMs = 1_000)
            advanceTimeBy(1_001)
            assertEquals(PlaybackOverlay.None, state.value)
        }

    @Test
    fun `switching overlays before the timeout keeps the newest one`() =
        runTest {
            val state = build()

            state.showAutoHiding(PlaybackOverlay.Info, timeoutMs = 1_000)
            advanceTimeBy(500)
            state.set(PlaybackOverlay.Panel)
            advanceTimeBy(2_000)

            assertEquals(PlaybackOverlay.Panel, state.value)
        }

    @Test
    fun `keep-alive restarts only an active countdown`() =
        runTest {
            val state = build()
            state.keepAlive()
            assertEquals(PlaybackOverlay.None, state.value)

            state.showAutoHiding(PlaybackOverlay.Info, timeoutMs = 1_000)
            advanceTimeBy(900)
            state.keepAlive()
            advanceTimeBy(900)
            assertEquals(PlaybackOverlay.Info, state.value)
            advanceTimeBy(101)
            assertEquals(PlaybackOverlay.None, state.value)

            state.set(PlaybackOverlay.Panel)
            state.keepAlive()
            advanceTimeBy(10_000)
            assertEquals(PlaybackOverlay.Panel, state.value)
        }

    @Test
    fun `a transient overlay promoted to another restarts with the new timeout`() =
        runTest {
            val state = build()

            state.showAutoHiding(PlaybackOverlay.Info, timeoutMs = 1_000)
            advanceTimeBy(900)
            state.showAutoHiding(PlaybackOverlay.InfoTransport, timeoutMs = 1_000)
            advanceTimeBy(900)
            assertEquals(PlaybackOverlay.InfoTransport, state.value)
            advanceTimeBy(101)
            assertEquals(PlaybackOverlay.None, state.value)
        }
}
