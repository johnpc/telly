package com.johncorser.telly.features.guide

import com.johncorser.telly.features.mylist.InMemoryMyListStore
import com.johncorser.telly.features.mylist.MyListMenu
import com.johncorser.telly.features.mylist.MyListMenuHost
import com.johncorser.telly.features.mylist.MyListProgramme
import com.johncorser.telly.features.playback.ChannelActions
import com.johncorser.telly.features.playback.PlayerMenuItem
import com.johncorser.telly.features.player.external.ExternalPlayer
import com.johncorser.telly.testutil.FakeChannelDao
import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.testProgram
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Row-by-row routing of the long-OK context sheet (catalogue §3 38-42). */
@OptIn(ExperimentalCoroutinesApi::class)
class GuideMenuControllerTest {
    private val dao = FakeChannelDao(listOf(testChannel(1, 1, "News One")))
    private val zapped = mutableListOf<Long>()
    private val externalOpens = mutableListOf<String>()
    private var searches = 0
    private var settingsOpens = 0
    private var row: GuideRow? = GuideRow(dao.channels.value.first(), 1, emptyList())
    private var infoData: GuideInfoData? =
        GuideInfoData(
            title = "Business Hour. S1 E7",
            range = "02:30 — 03:45 PM",
            remaining = null,
            progressPermille = null,
            description = "All-new episode.",
            group = "News",
            favorite = false,
        )

    private val myListStore = InMemoryMyListStore()
    private var manageOpens = 0
    private val reorderGroups = mutableListOf<String>()
    private var programme: MyListProgramme? =
        MyListProgramme(title = "Business Hour. S1 E7", startMs = 1_000L, endMs = 2_000L, description = "All-new.")

    private fun TestScope.build(focusMemory: GuideFocusMemory? = null): GuideMenuController {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val host =
            MyListMenuHost(
                menu = MyListMenu(myListStore, { 42L }, scope),
                programmeFor = { _ -> programme },
                group = { "News" },
                openManageFavorites = { manageOpens += 1 },
                openReorderChannels = { reorderGroups += it },
            )
        return GuideMenuController(
            channelActions =
                GuideSheetChannelActions(
                    ChannelActions(dao, scope, myList = host),
                    zapAway = { zapped += it.id },
                ),
            focusedRow = { row },
            info = { infoData },
            callbacks =
                GuideCallbacks(
                    onFullscreen = {},
                    onOpenSearch = { searches += 1 },
                    onOpenSettings = { settingsOpens += 1 },
                    external =
                        ExternalPlayer(enabledForTuning = { false }, launch = { url ->
                            externalOpens += url
                            true
                        }),
                ),
            focusMemory = focusMemory,
        )
    }

    private fun TestScope.buildOpenSheet(): GuideMenuController = build().apply { openRowMenu() }

    @Test
    fun `opening the sheet records the origin channel for the scrim exemption`() {
        runTest {
            val engine = GuideFocusEngine(GuideTestData.originMs, pastFloorDp = { 0f })
            val cells = listOf(GuideTestData.cell(GuideTestData.at(14, 30), GuideTestData.at(15, 30)))
            val focused = row!!.copy(cells = cells)
            row = focused
            engine.ensureFocus(listOf(focused), GuideTestData.nowMs)
            val menu = build(focusMemory = GuideFocusMemory(engine) { listOf(focused) })
            assertNull(menu.sheetChannelId)

            menu.openRowMenu()

            assertEquals(1L, menu.sheetChannelId)
        }
    }

    @Test
    fun `without focus memory there is no sheet origin channel`() {
        runTest {
            assertNull(buildOpenSheet().sheetChannelId)
        }
    }

    @Test
    fun `the sheet only opens while a row is focused`() {
        runTest {
            val menu = build()

            row = null
            menu.openRowMenu()
            assertEquals(GuideLayer.Grid, menu.layer.value)

            row = GuideRow(dao.channels.value.first(), 1, emptyList())
            menu.openRowMenu()
            assertEquals(GuideLayer.RowMenu, menu.layer.value)
        }
    }

    @Test
    fun `search closes the sheet and opens the search screen`() {
        runTest {
            val menu = buildOpenSheet()

            menu.onMenuItem(PlayerMenuItem.SEARCH)

            assertEquals(1, searches)
            assertEquals(GuideLayer.Grid, menu.layer.value)
        }
    }

    @Test
    fun `settings opens the settings sheet over the still-open menu`() {
        runTest {
            val menu = buildOpenSheet()

            menu.onMenuItem(PlayerMenuItem.SETTINGS)

            assertEquals(1, settingsOpens)
            assertEquals(GuideLayer.RowMenu, menu.layer.value)
        }
    }

    @Test
    fun `favorites toggles the row's channel and returns to the grid`() {
        runTest {
            val menu = buildOpenSheet()

            menu.onMenuItem(PlayerMenuItem.ADD_TO_FAVORITES)

            assertTrue(dao.channels.value.single().flags.favorite)
            assertEquals(GuideLayer.Grid, menu.layer.value)
        }
    }

    @Test
    fun `hide zaps the preview away then hides the channel`() {
        runTest {
            val menu = buildOpenSheet()

            menu.onMenuItem(PlayerMenuItem.HIDE_CHANNEL)

            assertEquals(listOf(1L), zapped)
            assertTrue(dao.channels.value.single().flags.hidden)
            assertEquals(GuideLayer.Grid, menu.layer.value)
        }
    }

    @Test
    fun `open in external player fires the chooser and returns to the grid`() {
        runTest {
            val menu = buildOpenSheet()

            menu.onMenuItem(PlayerMenuItem.OPEN_IN_EXTERNAL_PLAYER)

            assertEquals(listOf("http://s/1.ts"), externalOpens)
            assertEquals(GuideLayer.Grid, menu.layer.value)
        }
    }

    @Test
    fun `program description renders the info pane's title and text`() {
        runTest {
            val menu = buildOpenSheet()

            menu.onMenuItem(PlayerMenuItem.PROGRAM_DESCRIPTION)

            assertEquals(GuideLayer.Description("Business Hour. S1 E7", "All-new episode."), menu.layer.value)
        }
    }

    @Test
    fun `program description falls back to the no-information placeholder`() {
        runTest {
            val menu = buildOpenSheet()

            infoData = null
            menu.onMenuItem(PlayerMenuItem.PROGRAM_DESCRIPTION)

            assertEquals(GuideLayer.Description("No information", "No information"), menu.layer.value)
        }
    }

    @Test
    fun `every unbuilt row lands on coming-soon and back pops to the sheet`() {
        runTest {
            // The remaining formerly-premium reference rows and the
            // uncaptured rows share one fate: telly has no paywall, so both
            // open the branded coming-soon placeholder backing to the sheet
            // (the My-list/favorites-management rows are real now).
            val unbuilt =
                listOf(
                    PlayerMenuItem.BLOCK_CHANNEL,
                    PlayerMenuItem.ASSIGN_EPG,
                    PlayerMenuItem.MANAGE_BLOCKING,
                    PlayerMenuItem.MANAGE_VISIBILITY,
                    PlayerMenuItem.COPY_CHANNELS,
                    PlayerMenuItem.CREATE_GROUP,
                    PlayerMenuItem.GROUP_OPTIONS,
                )
            val menu = buildOpenSheet()

            unbuilt.forEach { item ->
                menu.onMenuItem(item)
                assertEquals(GuideLayer.ComingSoon(item.label, back = GuideLayer.RowMenu), menu.layer.value)
                menu.close()
                assertEquals(GuideLayer.RowMenu, menu.layer.value)
            }
        }
    }

    @Test
    fun `channel options replaces the sheet and back lands directly on the grid`() {
        runTest {
            val menu = buildOpenSheet()

            menu.onMenuItem(PlayerMenuItem.CHANNEL_OPTIONS)
            assertEquals(GuideLayer.ChannelOptions("News One"), menu.layer.value)

            // ref-round6 §A: the pane replaced the sheet — one BACK → grid.
            menu.close()
            assertEquals(GuideLayer.Grid, menu.layer.value)
        }
    }

    @Test
    fun `back from channel options restores the saved grid focus`() {
        runTest {
            val memory = mockk<GuideFocusMemory>(relaxed = true)
            val menu = build(memory).apply { openRowMenu() }
            verify(exactly = 1) { memory.save() }

            menu.onMenuItem(PlayerMenuItem.CHANNEL_OPTIONS)
            verify(exactly = 0) { memory.restore() }

            menu.close()
            verify(exactly = 1) { memory.restore() }
        }
    }

    @Test
    fun `channel-options rows are all locked and open coming-soon over the pane`() {
        runTest {
            val menu = buildOpenSheet()
            menu.onMenuItem(PlayerMenuItem.CHANNEL_OPTIONS)

            // Every §41 row is locked; activating one opens the branded
            // coming-soon placeholder that backs to the pane (no paywall).
            menu.onChannelOption("channel_options.name")
            assertEquals(
                GuideLayer.ComingSoon("channel_options.name", back = GuideLayer.ChannelOptions("News One")),
                menu.layer.value,
            )

            menu.close()
            assertEquals(GuideLayer.ChannelOptions("News One"), menu.layer.value)
        }
    }

    @Test
    fun `menu items are ignored once the focused row is gone`() {
        runTest {
            val menu = buildOpenSheet()

            row = null
            menu.onMenuItem(PlayerMenuItem.ADD_TO_FAVORITES)

            assertEquals(GuideLayer.RowMenu, menu.layer.value)
            assertTrue(dao.channels.value.none { it.flags.favorite })
        }
    }

    @Test
    fun `pushed rows remember themselves so back re-focuses them on the sheet`() {
        runTest {
            val menu = buildOpenSheet()
            assertNull(menu.sheetFocus.restore)

            menu.onMenuItem(PlayerMenuItem.PROGRAM_DESCRIPTION)
            menu.close()

            assertEquals(GuideLayer.RowMenu, menu.layer.value)
            assertEquals(PlayerMenuItem.PROGRAM_DESCRIPTION, menu.sheetFocus.restore)
        }
    }

    @Test
    fun `channel options leaves no refocus memory since it never returns to the sheet`() {
        runTest {
            val menu = buildOpenSheet()

            menu.onMenuItem(PlayerMenuItem.CHANNEL_OPTIONS)

            assertNull(menu.sheetFocus.restore)
        }
    }

    @Test
    fun `rows that leave the sheet clear the refocus memory`() {
        runTest {
            val menu = buildOpenSheet()
            menu.onMenuItem(PlayerMenuItem.ADD_TO_MY_LIST)
            menu.close()

            menu.onMenuItem(PlayerMenuItem.SETTINGS)

            assertNull(menu.sheetFocus.restore)
        }
    }

    @Test
    fun `reopening the sheet lands on the first row again`() {
        runTest {
            val menu = buildOpenSheet()
            menu.onMenuItem(PlayerMenuItem.PROGRAM_DESCRIPTION)
            menu.close()
            menu.close()

            menu.openRowMenu()

            assertNull(menu.sheetFocus.restore)
        }
    }

    @Test
    fun `cell dropdown rows open coming-soon and back to the grid`() {
        runTest {
            val menu = build()

            menu.onCellAction(GuideCellAction.PROGRAM_DESCRIPTION)
            assertEquals(GuideLayer.ComingSoon("Program description"), menu.layer.value)

            menu.close()
            assertEquals(GuideLayer.Grid, menu.layer.value)
        }
    }

    @Test
    fun `remind without the reminders seam still lands on coming-soon`() {
        runTest {
            val menu = build()

            menu.onCellAction(GuideCellAction.REMIND)

            assertEquals(GuideLayer.ComingSoon("Remind"), menu.layer.value)
        }
    }

    @Test
    fun `add to my list on the sheet saves the focused programme and returns to the grid`() {
        runTest {
            val menu = buildOpenSheet()

            menu.onMenuItem(PlayerMenuItem.ADD_TO_MY_LIST)

            assertEquals(listOf(1_000L), myListStore.entries.first().map { it.startMs })
            assertEquals(GuideLayer.Grid, menu.layer.value)
        }
    }

    @Test
    fun `selecting add to my list again removes the saved programme`() {
        runTest {
            val menu = buildOpenSheet()
            menu.onMenuItem(PlayerMenuItem.ADD_TO_MY_LIST)

            menu.openRowMenu()
            menu.onMenuItem(PlayerMenuItem.ADD_TO_MY_LIST)

            assertEquals(emptyList<Any>(), myListStore.entries.first())
        }
    }

    @Test
    fun `the cell dropdown's add to my list toggles the cell programme`() {
        runTest {
            val menu = build()
            val program = testProgram("tvg-1", 5_000L, 6_000L, "Late Show")
            menu.show(GuideLayer.CellMenu(GuideCell(5_000L, 6_000L, program)))

            menu.onCellAction(GuideCellAction.ADD_TO_MY_LIST)

            assertEquals(listOf(5_000L), myListStore.entries.first().map { it.startMs })
            assertEquals(GuideLayer.Grid, menu.layer.value)
        }
    }

    @Test
    fun `add to my list on a no-information filler cell falls back to coming-soon`() {
        runTest {
            val menu = build()
            menu.show(GuideLayer.CellMenu(GuideCell(5_000L, 6_000L, program = null)))

            menu.onCellAction(GuideCellAction.ADD_TO_MY_LIST)

            assertEquals(emptyList<Any>(), myListStore.entries.first())
            assertEquals(GuideLayer.ComingSoon("Add to My list"), menu.layer.value)
        }
    }

    @Test
    fun `manage favorites closes the sheet and opens the management screen`() {
        runTest {
            val menu = buildOpenSheet()

            menu.onMenuItem(PlayerMenuItem.MANAGE_FAVORITES)

            assertEquals(1, manageOpens)
            assertEquals(GuideLayer.Grid, menu.layer.value)
        }
    }

    @Test
    fun `reorder channels opens the reorder screen on the current group`() {
        runTest {
            val menu = buildOpenSheet()

            menu.onMenuItem(PlayerMenuItem.REORDER_CHANNELS)

            assertEquals(listOf("News"), reorderGroups)
            assertEquals(GuideLayer.Grid, menu.layer.value)
        }
    }
}
