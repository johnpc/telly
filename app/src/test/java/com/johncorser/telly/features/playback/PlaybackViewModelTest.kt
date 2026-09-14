package com.johncorser.telly.features.playback

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
    fun `up opens the info overlay and a second up expands the transport row`() =
        runTest {
            val vm = buildVm()

            assertTrue(vm.onKey(PlaybackKey.UP))
            assertEquals(PlaybackOverlay.Info, vm.overlay.value)

            assertTrue(vm.onKey(PlaybackKey.UP))
            assertEquals(PlaybackOverlay.InfoTransport, vm.overlay.value)

            advanceTimeBy(PlaybackViewModel.INFO_OVERLAY_TIMEOUT_MS + 1)
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

            advanceTimeBy(1_200)
            assertEquals(PlaybackOverlay.None, vm.overlay.value)
        }

    @Test
    fun `channel up zaps forward with wrap-around and shows the zap overlay`() =
        runTest {
            val vm = buildVm()

            vm.onKey(PlaybackKey.CHANNEL_UP)
            assertEquals(2L, vm.current.value?.id)
            assertEquals(PlaybackOverlay.ZapInfo, vm.overlay.value)

            vm.onKey(PlaybackKey.CHANNEL_UP)
            vm.onKey(PlaybackKey.CHANNEL_UP)
            assertEquals(1L, vm.current.value?.id)
            assertEquals(1L, store.getLong(TuneController.LAST_CHANNEL_KEY))
        }

    @Test
    fun `the zap overlay hides after its own longer timeout`() =
        runTest {
            val vm = buildVm()

            vm.onKey(PlaybackKey.CHANNEL_DOWN)
            assertEquals(3L, vm.current.value?.id)

            advanceTimeBy(PlaybackViewModel.ZAP_OVERLAY_TIMEOUT_MS - 1)
            assertEquals(PlaybackOverlay.ZapInfo, vm.overlay.value)
            advanceTimeBy(2)
            assertEquals(PlaybackOverlay.None, vm.overlay.value)
        }

    @Test
    fun `ok during the zap overlay promotes it to the full info overlay`() =
        runTest {
            val vm = buildVm()

            vm.onKey(PlaybackKey.CHANNEL_UP)
            assertTrue(vm.onKey(PlaybackKey.OK))

            assertEquals(PlaybackOverlay.Info, vm.overlay.value)
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
            assertEquals(PlaybackOverlay.QuickBar, vm.overlay.value)
            vm.onKey(PlaybackKey.BACK)
            assertEquals(PlaybackOverlay.None, vm.overlay.value)
        }

    @Test
    fun `long ok opens the quick-bar which auto-hides`() =
        runTest {
            val vm = buildVm()

            assertFalse(vm.onKey(PlaybackKey.LEFT))
            assertFalse(vm.onKey(PlaybackKey.RIGHT))
            assertTrue(vm.onKey(PlaybackKey.LONG_OK))
            assertEquals(PlaybackOverlay.QuickBar, vm.overlay.value)

            advanceTimeBy(PlaybackViewModel.QUICK_BAR_TIMEOUT_MS + 1)
            assertEquals(PlaybackOverlay.None, vm.overlay.value)
        }

    @Test
    fun `the quick-bar routes channels list to the panel and the rest to placeholders`() =
        runTest {
            val vm = buildVm()
            vm.onKey(PlaybackKey.MENU)

            vm.onQuickBarItem(QuickBarAction.CHANNELS_LIST)
            assertEquals(PlaybackOverlay.Panel, vm.overlay.value)

            vm.onQuickBarItem(QuickBarAction.SEARCH)
            assertEquals(PlaybackOverlay.ComingSoon("Search"), vm.overlay.value)
        }

    @Test
    fun `quick-bar items read the live stream details`() =
        runTest {
            val vm = buildVm()
            engine.video.value = VideoDetails(1280, 720, 25f, 1)

            val labels = vm.quickBarItems().map { it.label }

            assertEquals(
                listOf(
                    "Search", "Channels list", "Recordings", "Multiview",
                    "Picture-in-picture", "1280 × 720", "Mono", "0 ms", "Off",
                ),
                labels,
            )
        }

    @Test
    fun `tuning from the panel shows the zap overlay and persists the channel`() =
        runTest {
            val vm = buildVm()
            vm.onKey(PlaybackKey.BACK)

            vm.tuneFromPanel(channels[2])

            assertEquals(PlaybackOverlay.ZapInfo, vm.overlay.value)
            assertEquals(3L, vm.current.value?.id)
            assertEquals(3L, store.getLong(TuneController.LAST_CHANNEL_KEY))
            assertEquals("http://s/3.ts", engine.loaded.last())
        }

    @Test
    fun `the favorites row of a channel menu toggles and returns to the panel`() =
        runTest {
            val vm = buildVm()
            vm.onKey(PlaybackKey.BACK)
            vm.showChannelMenu(channels[0])

            vm.menu.onMenuItem(PlayerMenuItem.ADD_TO_FAVORITES)

            assertTrue(dao.channels.value.first { it.id == 1L }.flags.favorite)
            assertEquals(PlaybackOverlay.Panel, vm.overlay.value)

            vm.showChannelMenu(channels[0])
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
            vm.onKey(PlaybackKey.BACK)
            vm.showChannelMenu(channels[0])

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
            assertEquals("Description of Business Hour", info.description)
            assertEquals("16:40", info.elapsed)
            assertEquals("33:20", info.duration)
        }
}
