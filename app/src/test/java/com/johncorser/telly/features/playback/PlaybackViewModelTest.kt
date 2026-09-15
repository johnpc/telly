package com.johncorser.telly.features.playback

import com.johncorser.telly.features.player.VideoDetails
import com.johncorser.telly.testutil.testProgram
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackViewModelTest : PlaybackVmHarness() {
    @Test
    fun `the settings menu row opens the settings shell`() =
        runTest {
            var opened = false
            val vm = buildVm(hooks = PlaybackHooks(onOpenSettings = { opened = true }))

            vm.menu.onMenuItem(PlayerMenuItem.SETTINGS)

            assertTrue(opened)
            assertEquals(PlaybackOverlay.None, vm.overlay.value)
        }

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
    fun `background stops the stream and resume leaves fullscreen for the guide`() =
        runTest {
            val vm = buildVm()
            assertEquals(listOf("http://s/1.ts"), engine.loaded)

            vm.lifecycle.onBackground()
            assertEquals(1, engine.stops)

            vm.lifecycle.onForeground()

            assertEquals(1, exitedToGuide)
            assertEquals(listOf("http://s/1.ts"), engine.loaded)
        }

    @Test
    fun `foreground without a preceding stop keeps the running stream untouched`() =
        runTest {
            val vm = buildVm()

            vm.lifecycle.onForeground()

            assertEquals(listOf("http://s/1.ts"), engine.loaded)
            assertEquals(0, engine.stops)
            assertEquals(0, exitedToGuide)
        }

    @Test
    fun `a background stop before any tune is a no-op`() =
        runTest {
            dao.channels.value = emptyList()
            val vm = buildVm()

            vm.lifecycle.onBackground()
            vm.lifecycle.onForeground()

            assertEquals(0, engine.stops)
            assertEquals(0, exitedToGuide)
            assertTrue(engine.loaded.isEmpty())
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

            advanceTimeBy(1_500)
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
    fun `back at bare playback returns to the TV guide`() =
        runTest {
            val vm = buildVm()

            assertTrue(vm.onKey(PlaybackKey.BACK))

            assertEquals(1, exitedToGuide)
            assertEquals(PlaybackOverlay.None, vm.overlay.value)
        }

    @Test
    fun `the overlay's TV guide card leaves for the guide too`() =
        runTest {
            val vm = buildVm()
            vm.onKey(PlaybackKey.OK)

            vm.exitToGuide()

            assertEquals(1, exitedToGuide)
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

            // Search is real now (PlaybackSearchWiringTest); Recordings is not yet.
            vm.onQuickBarItem(QuickBarAction.RECORDINGS)
            assertEquals(PlaybackOverlay.ComingSoon("Recordings"), vm.overlay.value)
        }

    @Test
    fun `the quick-bar's multiview slot opens the multiview route`() =
        runTest {
            var opened = 0
            val vm = buildVm(hooks = PlaybackHooks(onOpenMultiview = { opened += 1 }))
            vm.onKey(PlaybackKey.MENU)

            vm.onQuickBarItem(QuickBarAction.MULTIVIEW)

            assertEquals(1, opened)
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
            vm.openPanel()
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
            vm.openPanel()
            vm.showChannelMenu(channels[1])

            vm.onKey(PlaybackKey.BACK)

            assertEquals(PlaybackOverlay.Panel, vm.overlay.value)
        }

    @Test
    fun `uncaptured menu rows keep the placeholder and back pops to the sheet`() =
        runTest {
            val vm = buildVm()
            vm.openPanel()
            vm.showChannelMenu(channels[0])

            vm.menu.onMenuItem(PlayerMenuItem.ASSIGN_EPG)

            val sheet = PlaybackOverlay.ChannelMenu(1L)
            assertEquals(PlaybackOverlay.ComingSoon("Assign EPG", back = sheet), vm.overlay.value)
            vm.onKey(PlaybackKey.BACK)
            assertEquals(sheet, vm.overlay.value)
            vm.onKey(PlaybackKey.BACK)
            assertEquals(PlaybackOverlay.Panel, vm.overlay.value)
        }

    @Test
    fun `unbuilt menu rows open coming-soon and back pops to the sheet`() =
        runTest {
            // telly has no premium tier: the reference's paywall rows now
            // share the coming-soon placeholder with the uncaptured rows.
            val vm = buildVm()
            vm.openPanel()
            vm.showChannelMenu(channels[0])

            vm.menu.onMenuItem(PlayerMenuItem.RECORD)

            val sheet = PlaybackOverlay.ChannelMenu(1L)
            assertEquals(PlaybackOverlay.ComingSoon("Record", back = sheet), vm.overlay.value)
            vm.onKey(PlaybackKey.BACK)
            assertEquals(sheet, vm.overlay.value)
        }

    @Test
    fun `the program description row shows the row's airing programme synopsis`() =
        runTest {
            programs.programs.value =
                listOf(testProgram("tvg-1", 900_000L, 1_100_000L, "Business Hour"))
            val vm = buildVm()
            vm.openPanel()
            vm.showChannelMenu(channels[0])

            vm.menu.onMenuItem(PlayerMenuItem.PROGRAM_DESCRIPTION)

            assertEquals(
                PlaybackOverlay.Description(
                    title = "Business Hour",
                    text = "Description of Business Hour",
                    back = PlaybackOverlay.ChannelMenu(1L),
                ),
                vm.overlay.value,
            )
            vm.onKey(PlaybackKey.BACK)
            assertEquals(PlaybackOverlay.ChannelMenu(1L), vm.overlay.value)
        }

    @Test
    fun `the program description row falls back to no-information without EPG`() =
        runTest {
            val vm = buildVm()
            vm.openPanel()
            vm.showChannelMenu(channels[2])

            vm.menu.onMenuItem(PlayerMenuItem.PROGRAM_DESCRIPTION)

            assertEquals(
                PlaybackOverlay.Description(
                    title = "No information",
                    text = "No information",
                    back = PlaybackOverlay.ChannelMenu(3L),
                ),
                vm.overlay.value,
            )
        }

    @Test
    fun `channel options replaces the sheet and its locked rows open coming-soon`() =
        runTest {
            val vm = buildVm()
            vm.openPanel()
            vm.showChannelMenu(channels[0])

            vm.menu.onMenuItem(PlayerMenuItem.CHANNEL_OPTIONS)

            // The pane REPLACES the sheet (ref-round6 §A): BACK targets the
            // panel directly, never the channel menu.
            val pane = PlaybackOverlay.ChannelOptions("News One", back = PlaybackOverlay.Panel)
            assertEquals(pane, vm.overlay.value)

            // Every §41 row is locked; activating one opens coming-soon over the pane.
            vm.menu.onChannelOption("channel_options.name")
            assertEquals(PlaybackOverlay.ComingSoon("channel_options.name", back = pane), vm.overlay.value)

            vm.onKey(PlaybackKey.BACK)
            assertEquals(pane, vm.overlay.value)
            vm.onKey(PlaybackKey.BACK)
            assertEquals(PlaybackOverlay.Panel, vm.overlay.value)
        }

    @Test
    fun `back from a pushed screen re-focuses the sheet row that pushed it`() =
        runTest {
            val vm = buildVm()
            vm.openPanel()
            vm.showChannelMenu(channels[0])
            assertNull(vm.menu.sheetFocus.restore)

            vm.menu.onMenuItem(PlayerMenuItem.PROGRAM_DESCRIPTION)
            vm.onKey(PlaybackKey.BACK)

            assertEquals(PlaybackOverlay.ChannelMenu(1L), vm.overlay.value)
            assertEquals(PlayerMenuItem.PROGRAM_DESCRIPTION, vm.menu.sheetFocus.restore)

            // Reopening the sheet fresh lands on the first row again.
            vm.onKey(PlaybackKey.BACK)
            vm.showChannelMenu(channels[0])
            assertNull(vm.menu.sheetFocus.restore)
        }

    @Test
    fun `the overlay's History card pushes the History screen`() =
        runTest {
            val vm = buildVm()
            vm.onKey(PlaybackKey.OK)

            vm.openHistory()

            assertEquals(1, openedHistory)
            assertEquals(0, exitedToGuide)
        }

    @Test
    fun `recent cards list watched channels newest-first without the playing one`() =
        runTest {
            programs.programs.value =
                listOf(
                    testProgram("tvg-1", 0, 2_000_000, "Business Hour", episode = "S1 E7"),
                    testProgram("tvg-2", 0, 2_000_000, "The Daily Brief"),
                )
            val vm = buildVm()

            now = 1_000_001L
            vm.onKey(PlaybackKey.CHANNEL_UP)
            now = 1_000_002L
            vm.onKey(PlaybackKey.CHANNEL_UP)

            assertEquals(3L, vm.current.value?.id)
            assertEquals(listOf(2L, 1L), vm.recents.cards.value.map { it.channel.id })
            assertEquals(listOf("The Daily Brief", "Business Hour. S1 E7"), vm.recents.cards.value.map { it.nowTitle })
            assertEquals("12:00 — 12:33 AM", vm.recents.cards.value[0].nowRange)
        }

    @Test
    fun `ok on a recent card tunes it with the zap overlay - the documented deviation`() =
        runTest {
            val vm = buildVm()
            vm.onKey(PlaybackKey.CHANNEL_UP)
            val card = vm.recents.cards.value.first()

            vm.recents.tune(card)

            assertEquals(card.channel.id, vm.current.value?.id)
            assertEquals(PlaybackOverlay.ZapInfo, vm.overlay.value)
            // The freshly tuned channel leaves the recent row.
            assertFalse(vm.recents.cards.value.any { it.channel.id == card.channel.id })
        }

    @Test
    fun `the clear card empties the watch history and keeps the overlay alive`() =
        runTest {
            val vm = buildVm()
            vm.onKey(PlaybackKey.CHANNEL_UP)
            vm.onKey(PlaybackKey.OK)
            assertEquals(1, vm.recents.cards.value.size)

            vm.recents.clear()

            assertEquals(emptyList<RecentCard>(), vm.recents.cards.value)
            assertEquals(emptyMap<String, Long>(), historyDao.events.value)
            assertEquals(PlaybackOverlay.Info, vm.overlay.value)
        }

    @Test
    fun `every tune records a deduped newest-first watch history`() =
        runTest {
            val vm = buildVm()

            now = 1_000_001L
            vm.onKey(PlaybackKey.CHANNEL_UP)
            now = 1_000_002L
            vm.onKey(PlaybackKey.CHANNEL_UP)
            now = 1_000_003L
            vm.tuneFromPanel(channels[1])

            assertEquals(2L, vm.current.value?.id)
            assertEquals(
                mapOf("tvg-1" to 1_000_000L, "tvg-2" to 1_000_003L, "tvg-3" to 1_000_002L),
                historyDao.events.value,
            )
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
