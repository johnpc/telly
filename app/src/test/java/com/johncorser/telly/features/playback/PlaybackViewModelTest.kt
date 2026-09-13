package com.johncorser.telly.features.playback

import com.johncorser.telly.features.panel.PanelViewModel
import com.johncorser.telly.features.player.VideoDetails
import com.johncorser.telly.testutil.FakeChannelDao
import com.johncorser.telly.testutil.FakeKeyValueStore
import com.johncorser.telly.testutil.FakePlayerEngine
import com.johncorser.telly.testutil.FakeProgramDao
import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.testEpgRepository
import com.johncorser.telly.testutil.testProgram
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class PlaybackViewModelTest {
    private val channels =
        listOf(
            testChannel(1, 1, "News One", group = "News"),
            testChannel(2, 2, "News Two", group = "News"),
            testChannel(3, 3, "Sports Arena", group = "Sports"),
        )
    private val dao = FakeChannelDao(channels)
    private val engine = FakePlayerEngine()
    private val store = FakeKeyValueStore()
    private val programs = FakeProgramDao()

    private fun TestScope.buildVm(clock: () -> Long = { 1_000_000L }): PlaybackViewModel =
        PlaybackViewModel(
            env =
                PlaybackEnv(
                    channelDao = dao,
                    epgRepository = testEpgRepository(programs),
                    engine = engine,
                    store = store,
                    clock = clock,
                    zone = TimeZone.getTimeZone("UTC"),
                ),
            scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler)),
        )

    @Test
    fun `cold start restores the last watched channel`() =
        runTest {
            store.putLong(TuneController.LAST_CHANNEL_KEY, 2)

            val vm = buildVm()

            assertEquals(2L, vm.current.value?.id)
            assertEquals(listOf("http://s/2.ts"), engine.loaded)
        }

    @Test
    fun `first run tunes the first channel and remembers it`() =
        runTest {
            val vm = buildVm()

            assertEquals(1L, vm.current.value?.id)
            assertEquals(listOf("http://s/1.ts"), engine.loaded)
            assertEquals(1L, store.getLong(TuneController.LAST_CHANNEL_KEY))
        }

    @Test
    fun `ok opens the info overlay which auto-hides after the timeout`() =
        runTest {
            val vm = buildVm()

            assertTrue(vm.onKey(PlaybackKey.OK))
            assertEquals(PlaybackOverlay.Info, vm.overlay.value)

            advanceTimeBy(PlaybackViewModel.INFO_OVERLAY_TIMEOUT_MS - 1)
            assertEquals(PlaybackOverlay.Info, vm.overlay.value)
            advanceTimeBy(2)
            assertEquals(PlaybackOverlay.None, vm.overlay.value)
        }

    @Test
    fun `browsing inside the overlay postpones the auto-hide`() =
        runTest {
            val vm = buildVm()
            vm.onKey(PlaybackKey.OK)

            advanceTimeBy(4_000)
            vm.onOverlayInteraction()
            advanceTimeBy(4_000)
            assertEquals(PlaybackOverlay.Info, vm.overlay.value)

            advanceTimeBy(1_100)
            assertEquals(PlaybackOverlay.None, vm.overlay.value)
        }

    @Test
    fun `channel up zaps forward with wrap-around and shows the info overlay`() =
        runTest {
            val vm = buildVm()

            vm.onKey(PlaybackKey.CHANNEL_UP)
            assertEquals(2L, vm.current.value?.id)
            assertEquals(PlaybackOverlay.Info, vm.overlay.value)

            vm.onKey(PlaybackKey.CHANNEL_UP)
            vm.onKey(PlaybackKey.CHANNEL_UP)
            assertEquals(1L, vm.current.value?.id)
            assertEquals(1L, store.getLong(TuneController.LAST_CHANNEL_KEY))
        }

    @Test
    fun `channel down wraps backwards from the first channel`() =
        runTest {
            val vm = buildVm()

            vm.onKey(PlaybackKey.CHANNEL_DOWN)

            assertEquals(3L, vm.current.value?.id)
        }

    @Test
    fun `up opens the panel focused on the previous channel`() =
        runTest {
            val vm = buildVm()

            vm.onKey(PlaybackKey.UP)

            assertEquals(PlaybackOverlay.Panel, vm.overlay.value)
            assertEquals(PanelViewModel.ALL_CHANNELS, vm.panel.selectedGroup.value)
            assertEquals(2, vm.panel.focusIndex.value)
            assertEquals(1L, vm.current.value?.id)
        }

    @Test
    fun `back at bare playback opens the panel at the tuned channel`() =
        runTest {
            val vm = buildVm()

            assertTrue(vm.onKey(PlaybackKey.BACK))

            assertEquals(PlaybackOverlay.Panel, vm.overlay.value)
            assertEquals(0, vm.panel.focusIndex.value)
        }

    @Test
    fun `back dismisses one overlay layer at a time`() =
        runTest {
            val vm = buildVm()

            vm.onKey(PlaybackKey.OK)
            assertTrue(vm.onKey(PlaybackKey.BACK))
            assertEquals(PlaybackOverlay.None, vm.overlay.value)

            vm.onKey(PlaybackKey.MENU)
            assertEquals(PlaybackOverlay.Menu, vm.overlay.value)
            vm.onKey(PlaybackKey.BACK)
            assertEquals(PlaybackOverlay.None, vm.overlay.value)
        }

    @Test
    fun `long ok opens the context menu and left right stay unconsumed`() =
        runTest {
            val vm = buildVm()

            assertFalse(vm.onKey(PlaybackKey.LEFT))
            assertFalse(vm.onKey(PlaybackKey.RIGHT))
            assertTrue(vm.onKey(PlaybackKey.LONG_OK))
            assertEquals(PlaybackOverlay.Menu, vm.overlay.value)
        }

    @Test
    fun `tuning from the panel dismisses it and persists the channel`() =
        runTest {
            val vm = buildVm()
            vm.onKey(PlaybackKey.UP)

            vm.tuneFromPanel(channels[2])

            assertEquals(PlaybackOverlay.None, vm.overlay.value)
            assertEquals(3L, vm.current.value?.id)
            assertEquals(3L, store.getLong(TuneController.LAST_CHANNEL_KEY))
            assertEquals("http://s/3.ts", engine.loaded.last())
        }

    @Test
    fun `the favorites menu row toggles and closes the player menu`() =
        runTest {
            val vm = buildVm()
            vm.onKey(PlaybackKey.MENU)

            vm.menu.onMenuItem(PlayerMenuItem.ADD_TO_FAVORITES)

            assertTrue(dao.channels.value.first { it.id == 1L }.flags.favorite)
            assertEquals(PlaybackOverlay.None, vm.overlay.value)

            vm.onKey(PlaybackKey.MENU)
            vm.menu.onMenuItem(PlayerMenuItem.ADD_TO_FAVORITES)
            assertFalse(dao.channels.value.first { it.id == 1L }.flags.favorite)
        }

    @Test
    fun `hiding the tuned channel from its channel menu retunes and returns to the panel`() =
        runTest {
            val vm = buildVm()
            vm.onKey(PlaybackKey.BACK)
            vm.showChannelMenu(channels[0])

            assertEquals(PlaybackOverlay.ChannelMenu(1L), vm.overlay.value)
            assertEquals(1L, vm.menu.menuChannel()?.id)

            vm.menu.onMenuItem(PlayerMenuItem.HIDE_CHANNEL)

            assertEquals(PlaybackOverlay.Panel, vm.overlay.value)
            assertTrue(dao.channels.value.first { it.id == 1L }.flags.hidden)
            assertEquals(2L, vm.current.value?.id)
            assertEquals(2, vm.panel.rows.value.size)
        }

    @Test
    fun `back from a channel menu returns to the panel`() =
        runTest {
            val vm = buildVm()
            vm.onKey(PlaybackKey.BACK)
            vm.showChannelMenu(channels[1])

            vm.onKey(PlaybackKey.BACK)

            assertEquals(PlaybackOverlay.Panel, vm.overlay.value)
        }

    @Test
    fun `unbuilt menu rows route to a branded coming-soon placeholder`() =
        runTest {
            val vm = buildVm()
            vm.onKey(PlaybackKey.MENU)

            vm.menu.onMenuItem(PlayerMenuItem.RECORD)

            assertEquals(PlaybackOverlay.ComingSoon("Record"), vm.overlay.value)
            vm.onKey(PlaybackKey.BACK)
            assertEquals(PlaybackOverlay.None, vm.overlay.value)
        }

    @Test
    fun `the overlay cards open the panel and the history placeholder`() =
        runTest {
            val vm = buildVm()

            vm.openPanel()
            assertEquals(PlaybackOverlay.Panel, vm.overlay.value)
            assertEquals(0, vm.panel.focusIndex.value)

            vm.showComingSoon("History")
            assertEquals(PlaybackOverlay.ComingSoon("History"), vm.overlay.value)
        }

    @Test
    fun `the info stream carries programme data and stream badges`() =
        runTest {
            programs.programs.value =
                listOf(
                    testProgram("tvg-1", 0, 2_000_000, "Business Hour", episode = "S1 E7"),
                    testProgram("tvg-1", 2_000_000, 3_000_000, "Newsroom Live", episode = "S1 E8"),
                )
            val vm = buildVm()
            engine.video.value = VideoDetails(1280, 720, 25f, 1)

            val info = vm.info.value!!

            assertEquals("Business Hour. S1 E7", info.title)
            assertEquals(500, info.progressPermille)
            assertEquals("17 min", info.remaining)
            assertEquals(listOf("HD", "25 FPS", "MONO"), info.badges)
            assertTrue(info.nextLine!!.endsWith("Newsroom Live. S1 E8"))
            assertEquals(1, info.number)
            assertEquals("News One", info.name)
        }
}
