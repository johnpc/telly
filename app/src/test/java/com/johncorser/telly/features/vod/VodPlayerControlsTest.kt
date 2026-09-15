package com.johncorser.telly.features.vod

import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import com.johncorser.telly.features.player.Media3PlayerEngine
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VodPlayerControlsTest {
    private val player = mockk<ExoPlayer>(relaxed = true)
    private val engine = Media3PlayerEngine(player)
    private var persists = 0

    private fun TestScope.controls(scope: CoroutineScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))) =
        scope to VodPlayerControls(engine, VodTransportVisibility(scope), persistOnPause = { persists++ })

    @Test
    fun `inert until begin`() =
        runTest {
            val (scope, controls) = controls()

            controls.togglePause()
            controls.seekBy(10_000)

            assertFalse(controls.transport.visible.value)
            verify(exactly = 0) { player.pause() }
            verify(exactly = 0) { player.seekTo(any<Long>()) }
            scope.cancel()
        }

    @Test
    fun `begin reveals the transport and it auto-hides while playing`() =
        runTest {
            val (scope, controls) = controls()

            controls.begin()
            assertTrue(controls.transport.visible.value)

            advanceTimeBy(5_100)
            assertFalse(controls.transport.visible.value)
            scope.cancel()
        }

    @Test
    fun `OK pauses, persists and keeps the transport up until unpaused`() =
        runTest {
            val (scope, controls) = controls()
            controls.begin()

            controls.togglePause()

            assertTrue(controls.paused.value)
            assertEquals(1, persists)
            verify { player.pause() }
            advanceTimeBy(10_000)
            assertTrue(controls.transport.visible.value)

            controls.togglePause()
            assertFalse(controls.paused.value)
            verify { player.play() }
            advanceTimeBy(5_100)
            assertFalse(controls.transport.visible.value)
            scope.cancel()
        }

    @Test
    fun `seeks clamp to the clip bounds`() =
        runTest {
            val (scope, controls) = controls()
            controls.begin()
            every { player.duration } returns 30_000L

            every { player.currentPosition } returns 3_000L
            controls.seekBy(-10_000)
            verify { player.seekTo(0L) }

            every { player.currentPosition } returns 25_000L
            controls.seekBy(10_000)
            verify { player.seekTo(30_000L) }
            scope.cancel()
        }

    @Test
    fun `an unknown duration seeks forward unclamped`() =
        runTest {
            val (scope, controls) = controls()
            controls.begin()
            every { player.duration } returns C.TIME_UNSET
            every { player.currentPosition } returns 1_000L

            controls.seekBy(10_000)

            verify { player.seekTo(11_000L) }
            scope.cancel()
        }

    @Test
    fun `sample publishes the transport clock`() =
        runTest {
            val (scope, controls) = controls()
            every { player.currentPosition } returns 12_000L
            every { player.duration } returns 30_000L

            controls.sample()

            assertEquals(VodProgress(12_000, 30_000), controls.progress.value)
            assertEquals(400, controls.progress.value.permille)
            scope.cancel()
        }
}
