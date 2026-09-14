package com.johncorser.telly.features.guide

import com.johncorser.telly.features.guide.GuideTestData.at
import com.johncorser.telly.features.guide.GuideTestData.nowMs
import com.johncorser.telly.features.guide.GuideTestData.utc
import com.johncorser.telly.features.history.HistoryGroup
import com.johncorser.telly.features.history.WatchHistory
import com.johncorser.telly.features.history.db.WatchHistoryEntity
import com.johncorser.telly.features.panel.PanelViewModel
import com.johncorser.telly.features.playback.PlaybackEnv
import com.johncorser.telly.features.playback.PlayerMenuItem
import com.johncorser.telly.features.playback.TuneController
import com.johncorser.telly.testutil.FakeChannelDao
import com.johncorser.telly.testutil.FakeKeyValueStore
import com.johncorser.telly.testutil.FakePlayerEngine
import com.johncorser.telly.testutil.FakeProgramDao
import com.johncorser.telly.testutil.FakeWatchHistoryDao
import com.johncorser.telly.testutil.describedAs
import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.testEpgRepository
import com.johncorser.telly.testutil.testProgram
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GuideControllerTest {
    private val channels =
        listOf(
            testChannel(1, 1, "News One", group = "News"),
            testChannel(2, 2, "News One HD", group = "News"),
            testChannel(3, 3, "Sports Arena", group = "Sports"),
            testChannel(4, 4, "Silent FM", group = "Music", tvgId = null),
        )
    private val dao = FakeChannelDao(channels)
    private val engine = FakePlayerEngine()
    private val store = FakeKeyValueStore()
    private val programs =
        FakeProgramDao(
            listOf(
                testProgram(
                    "tvg-1",
                    at(14, 30),
                    at(15, 45),
                    "Business Hour",
                    episode = "S1 E7",
                ).describedAs("All-new episode."),
                testProgram("tvg-1", at(15, 45), at(17, 15), "Newsroom Live"),
                testProgram("tvg-2", at(14, 10), at(15, 30), "The Daily Brief"),
                testProgram("tvg-2", at(15, 30), at(16, 0), "Global Update"),
                testProgram("tvg-3", at(13, 0), at(16, 0), "Boxing Classics"),
            ),
        )
    private var fullscreens = 0
    private var pastDays = 7
    private val historyDao = FakeWatchHistoryDao()

    private fun TestScope.buildController(initialGroup: String = PanelViewModel.ALL_CHANNELS): GuideController =
        GuideController(
            env =
                PlaybackEnv(
                    channelDao = dao,
                    epgRepository = testEpgRepository(programs),
                    engine = engine,
                    store = store,
                    clock = { nowMs },
                    zone = utc,
                ),
            history = WatchHistory(historyDao) { nowMs },
            pastDays = { pastDays },
            scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler)),
            callbacks =
                GuideCallbacks(
                    onFullscreen = { fullscreens += 1 },
                    onOpenSearch = {},
                    onOpenSettings = {},
                ),
            initialGroup = initialGroup,
        )

    private suspend fun watched(
        tvgId: String,
        atMs: Long,
    ) = historyDao.upsert(WatchHistoryEntity(tvgId, atMs))

    private fun focusedTitle(controller: GuideController): String? =
        controller.focus.value
            ?.cell
            ?.program
            ?.details
            ?.title

    @Test
    fun `rows load for all channels with epg-less strips and focus lands on the airing cell`() {
        runTest {
            val controller = buildController()

            assertEquals(4, controller.rows.value.size)
            assertEquals(listOf(1, 2, 3, 4), controller.rows.value.map { it.displayNumber })
            assertTrue(controller.rows.value.last().cells.none { it.hasInfo })
            assertEquals(0, controller.focus.value?.rowIndex)
            assertEquals("Business Hour", focusedTitle(controller))
            assertEquals("Sun, Sep 13, 2:38 PM", controller.clockText)
        }
    }

    @Test
    fun `the info pane follows the focused cell`() {
        runTest {
            val controller = buildController()

            assertEquals("Business Hour. S1 E7", controller.info.value?.title)
            assertEquals("All-new episode.", controller.info.value?.description)

            controller.onKey(GuideKey.RIGHT)
            assertEquals("Newsroom Live", controller.info.value?.title)
            assertNull(controller.info.value?.remaining)
        }
    }

    @Test
    fun `right pans the shared scroll and left comes back to the edge`() {
        runTest {
            val controller = buildController()

            assertTrue(controller.onKey(GuideKey.RIGHT))
            assertTrue(controller.scrollX.value > 0f)

            controller.onKey(GuideKey.LEFT)
            assertEquals(0f, controller.scrollX.value)
            assertEquals(GuideLayer.Grid, controller.layer.value)
        }
    }

    @Test
    fun `up and down keep the focused time roughly constant`() {
        runTest {
            val controller = buildController()

            controller.onKey(GuideKey.DOWN)

            val focus = controller.focus.value!!
            assertEquals(1, focus.rowIndex)
            assertEquals("The Daily Brief", focusedTitle(controller))
            assertTrue(focus.cell.contains(nowMs))
        }
    }

    @Test
    fun `left at the window edge opens the groups column and right closes it`() {
        runTest {
            val controller = buildController()

            assertTrue(controller.onKey(GuideKey.LEFT))

            assertEquals(GuideLayer.Groups, controller.layer.value)
            assertEquals(
                listOf("Favorites", "All channels", "News", "Sports", "Music"),
                controller.groups.value,
            )

            assertTrue(controller.onKey(GuideKey.RIGHT))
            assertEquals(GuideLayer.Grid, controller.layer.value)
        }
    }

    @Test
    fun `selecting a group renumbers from one and refocuses its airing cell`() {
        runTest {
            val controller = buildController()
            controller.onKey(GuideKey.LEFT)

            controller.selectGroup("Sports")

            assertEquals(GuideLayer.Grid, controller.layer.value)
            assertEquals(listOf("Sports Arena"), controller.rows.value.map { it.channel.source.name })
            assertEquals(listOf(1), controller.rows.value.map { it.displayNumber })
            assertEquals("Boxing Classics", focusedTitle(controller))
            assertEquals(0f, controller.scrollX.value)
        }
    }

    @Test
    fun `two-stage ok tunes the preview then goes fullscreen`() {
        runTest {
            val controller = buildController()
            controller.onKey(GuideKey.DOWN)

            assertTrue(controller.onKey(GuideKey.OK))

            assertEquals(2L, controller.preview.value?.id)
            assertEquals("http://s/2.ts", engine.loaded.last())
            assertEquals(2L, store.getLong(TuneController.LAST_CHANNEL_KEY))
            assertEquals(setOf("tvg-2"), historyDao.events.value.keys)
            assertEquals(0, fullscreens)

            assertTrue(controller.onKey(GuideKey.OK))
            assertEquals(1, fullscreens)
        }
    }

    @Test
    fun `ok on a future programme opens the dropdown and its rows hit the paywall`() {
        runTest {
            val controller = buildController()
            controller.onKey(GuideKey.RIGHT)

            controller.onKey(GuideKey.OK)

            val menu = controller.layer.value as GuideLayer.CellMenu
            assertEquals("Newsroom Live", menu.cell.program?.details?.title)
            assertEquals(
                listOf("Remind", "Record", "Custom recording", "Add to My list", "Program description"),
                GuideCellAction.entries.map { it.label },
            )

            controller.menu.onCellAction(GuideCellAction.REMIND)
            assertEquals(GuideLayer.Paywall("Remind"), controller.layer.value)

            assertTrue(controller.onKey(GuideKey.BACK))
            assertEquals(GuideLayer.Grid, controller.layer.value)
        }
    }

    @Test
    fun `long-ok and menu open the row context sheet and back returns to the grid`() {
        runTest {
            val controller = buildController()

            assertTrue(controller.onKey(GuideKey.LONG_OK))
            assertEquals(GuideLayer.RowMenu, controller.layer.value)

            assertTrue(controller.onKey(GuideKey.BACK))
            assertEquals(GuideLayer.Grid, controller.layer.value)

            assertTrue(controller.onKey(GuideKey.MENU))
            assertEquals(GuideLayer.RowMenu, controller.layer.value)
        }
    }

    @Test
    fun `the sheet's favorites row toggles the focused channel and returns to the grid`() {
        runTest {
            val controller = buildController()
            controller.onKey(GuideKey.LONG_OK)

            controller.menu.onMenuItem(PlayerMenuItem.ADD_TO_FAVORITES)

            assertTrue(dao.channels.value.first { it.id == 1L }.flags.favorite)
            assertEquals(GuideLayer.Grid, controller.layer.value)
        }
    }

    @Test
    fun `hiding the previewed channel from the sheet retunes the preview away`() {
        runTest {
            store.putLong(TuneController.LAST_CHANNEL_KEY, 1L)
            val controller = buildController()
            controller.onKey(GuideKey.LONG_OK)

            controller.menu.onMenuItem(PlayerMenuItem.HIDE_CHANNEL)

            assertTrue(dao.channels.value.first { it.id == 1L }.flags.hidden)
            assertEquals(2L, controller.preview.value?.id)
            assertEquals(GuideLayer.Grid, controller.layer.value)
        }
    }

    @Test
    fun `program description shows the focused programme's synopsis and back pops to the sheet`() {
        runTest {
            val controller = buildController()
            controller.onKey(GuideKey.LONG_OK)

            controller.menu.onMenuItem(PlayerMenuItem.PROGRAM_DESCRIPTION)

            assertEquals(GuideLayer.Description("Business Hour. S1 E7", "All-new episode."), controller.layer.value)
            assertTrue(controller.onKey(GuideKey.BACK))
            assertEquals(GuideLayer.RowMenu, controller.layer.value)
        }
    }

    @Test
    fun `back on the grid is unconsumed so guide root can exit the app`() {
        runTest {
            val controller = buildController()

            assertFalse(controller.onKey(GuideKey.BACK))
        }
    }

    @Test
    fun `the preview resumes the last-watched channel like returning from playback`() {
        runTest {
            store.putLong(TuneController.LAST_CHANNEL_KEY, 3L)

            val controller = buildController()

            assertEquals(3L, controller.preview.value?.id)
            assertEquals(listOf("http://s/3.ts"), engine.loaded)
        }
    }

    @Test
    fun `a day jump back lands on past cells within the past-days setting`() {
        runTest {
            pastDays = 7
            val controller = buildController()

            assertTrue(controller.onKey(GuideKey.LONG_LEFT))

            assertTrue(controller.scrollX.value < 0f)
            assertEquals(nowMs - GuideGeometry.DAY_MS, controller.focus.value?.anchorMs)
            assertFalse(controller.focus.value!!.cell.hasInfo)
        }
    }

    @Test
    fun `day jumps clamp when the past-days setting forbids history`() {
        runTest {
            pastDays = 0
            val controller = buildController()

            controller.onKey(GuideKey.LONG_LEFT)

            assertEquals(0f, controller.scrollX.value)
        }
    }

    @Test
    fun `a day jump forward pans a full day ahead`() {
        runTest {
            val controller = buildController()

            controller.onKey(GuideKey.LONG_RIGHT)

            assertEquals(48 * 160f, controller.scrollX.value)
            assertFalse(controller.focus.value!!.cell.hasInfo)
        }
    }

    @Test
    fun `the all-channels group is selected on open like a fresh TiviMate start`() {
        runTest {
            val controller = buildController()

            assertEquals(PanelViewModel.ALL_CHANNELS, controller.selectedGroup.value)
        }
    }

    @Test
    fun `the history source group opens selected with newest-first rows renumbered from one`() {
        runTest {
            watched("tvg-1", at(14, 0))
            watched("tvg-3", at(14, 20))

            val controller = buildController(initialGroup = HistoryGroup.NAME)

            assertEquals(HistoryGroup.NAME, controller.selectedGroup.value)
            assertEquals(listOf("Sports Arena", "News One"), controller.rows.value.map { it.channel.source.name })
            assertEquals(listOf(1, 2), controller.rows.value.map { it.displayNumber })
            assertEquals("Boxing Classics", focusedTitle(controller))
        }
    }

    @Test
    fun `history leads the groups column only while it is the source group`() {
        runTest {
            watched("tvg-1", at(14, 0))
            val controller = buildController(initialGroup = HistoryGroup.NAME)

            assertEquals(
                listOf("History", "Favorites", "All channels", "News", "Sports", "Music"),
                controller.groups.value,
            )

            controller.selectGroup("Sports")

            assertEquals(
                listOf("Favorites", "All channels", "News", "Sports", "Music"),
                controller.groups.value,
            )
            assertEquals(listOf(1), controller.rows.value.map { it.displayNumber })
        }
    }

    @Test
    fun `history keys without a matching channel drop from the rows`() {
        runTest {
            watched("tvg-gone", at(14, 30))
            watched("tvg-2", at(14, 0))

            val controller = buildController(initialGroup = HistoryGroup.NAME)

            assertEquals(listOf("News One HD"), controller.rows.value.map { it.channel.source.name })
        }
    }

    @Test
    fun `close releases the preview engine`() {
        runTest {
            val controller = buildController()

            controller.close()

            assertTrue(engine.released)
        }
    }
}
