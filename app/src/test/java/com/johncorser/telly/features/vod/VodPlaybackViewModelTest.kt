package com.johncorser.telly.features.vod

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.johncorser.telly.features.player.Media3PlayerEngine
import com.johncorser.telly.features.vod.db.VodItemEntity
import com.johncorser.telly.features.vod.db.VodPositionEntity
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
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class VodPlaybackViewModelTest {
    private val player = mockk<ExoPlayer>(relaxed = true)
    private val engine = Media3PlayerEngine(player)
    private val items = FakeVodItemDao()
    private val positions = FakeVodPositionDao()
    private var remember = true
    private var exits = 0

    private val item =
        VodItemEntity(
            playlistId = 1,
            sortIndex = 0,
            itemKey = "k",
            name = "Big Buck Bunny",
            streamUrl = "http://s/bbb.mp4",
        )

    private fun deps(): VodDeps = VodDeps(items, positions, { engine }, { remember }, { 99_000L })

    private fun TestScope.model(scope: CoroutineScope): VodPlaybackViewModel =
        VodPlaybackViewModel(deps(), "k", engine, scope, onExit = { exits++ })

    private fun TestScope.scope(): CoroutineScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))

    @Test
    fun `no stored position plays from the start`() =
        runTest {
            items.items.value = listOf(item)
            val scope = scope()
            val model = model(scope)

            model.start()

            assertEquals(VodStage.Playing, model.stage.value)
            assertTrue(model.controls.transport.visible.value)
            verify { player.prepare() }
            verify(exactly = 0) { player.seekTo(any<Long>()) }
            scope.cancel()
        }

    @Test
    fun `a stored mid-watch position offers resume and seeks on Resume`() =
        runTest {
            items.items.value = listOf(item)
            positions.upsert(VodPositionEntity("k", positionMs = 12_000, durationMs = 30_000, updatedAtMs = 1))
            val scope = scope()
            val model = model(scope)

            model.start()
            assertEquals(VodStage.ResumePrompt(12_000), model.stage.value)
            verify(exactly = 0) { player.prepare() }

            model.resumeStored()
            assertEquals(VodStage.Playing, model.stage.value)
            verify { player.seekTo(12_000L) }
            scope.cancel()
        }

    @Test
    fun `Start over plays from the beginning instead`() =
        runTest {
            items.items.value = listOf(item)
            positions.upsert(VodPositionEntity("k", positionMs = 12_000, durationMs = 30_000, updatedAtMs = 1))
            val scope = scope()
            val model = model(scope)
            model.start()

            model.startOver()

            assertEquals(VodStage.Playing, model.stage.value)
            verify(exactly = 0) { player.seekTo(any<Long>()) }
            scope.cancel()
        }

    @Test
    fun `positions outside the resume band skip the prompt`() =
        runTest {
            items.items.value = listOf(item)
            positions.upsert(VodPositionEntity("k", positionMs = 500, durationMs = 30_000, updatedAtMs = 1))
            val scope = scope()
            val model = model(scope)

            model.start()

            assertEquals(VodStage.Playing, model.stage.value)
            scope.cancel()
        }

    @Test
    fun `remember off never prompts even with a stored position`() =
        runTest {
            remember = false
            items.items.value = listOf(item)
            positions.upsert(VodPositionEntity("k", positionMs = 12_000, durationMs = 30_000, updatedAtMs = 1))
            val scope = scope()
            val model = model(scope)

            model.start()

            assertEquals(VodStage.Playing, model.stage.value)
            scope.cancel()
        }

    @Test
    fun `exit persists the position once and pops the route`() =
        runTest {
            items.items.value = listOf(item)
            every { player.currentPosition } returns 4_000L
            every { player.duration } returns 30_000L
            val scope = scope()
            val model = model(scope)
            model.start()

            model.exit()
            model.exit()

            val row = positions.rows.value.single()
            assertEquals(4_000L, row.positionMs)
            assertEquals(30_000L, row.durationMs)
            assertEquals(99_000L, row.updatedAtMs)
            assertEquals(1, exits)
            scope.cancel()
        }

    @Test
    fun `the position persists every ten seconds of playback`() =
        runTest {
            items.items.value = listOf(item)
            every { player.currentPosition } returns 9_500L
            every { player.duration } returns 30_000L
            val scope = scope()
            val model = model(scope)
            model.start()
            assertTrue(positions.rows.value.isEmpty())

            advanceTimeBy(10_100)

            assertEquals(9_500L, positions.rows.value.single().positionMs)
            scope.cancel()
        }

    @Test
    fun `media end clears the position as finished and exits the route`() =
        runTest {
            items.items.value = listOf(item)
            positions.upsert(VodPositionEntity("k", positionMs = 12_000, durationMs = 30_000, updatedAtMs = 1))
            every { player.currentPosition } returns 30_000L
            every { player.duration } returns 30_000L
            val scope = scope()
            val model = model(scope)
            model.start()
            model.startOver()

            engine.onPlaybackStateChanged(Player.STATE_ENDED)

            assertTrue(positions.rows.value.isEmpty())
            assertEquals(1, exits)
            scope.cancel()
        }

    @Test
    fun `a vanished item key just leaves the route`() =
        runTest {
            val scope = scope()
            val model = model(scope)

            model.start()

            assertEquals(VodStage.Loading, model.stage.value)
            assertEquals(1, exits)
            scope.cancel()
        }
}
