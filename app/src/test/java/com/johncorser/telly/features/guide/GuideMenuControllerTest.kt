package com.johncorser.telly.features.guide

import com.johncorser.telly.features.playback.ChannelActions
import com.johncorser.telly.features.playback.PlayerMenuItem
import com.johncorser.telly.features.settings.RowIds
import com.johncorser.telly.testutil.FakeChannelDao
import com.johncorser.telly.testutil.testChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
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

    private fun TestScope.build(): GuideMenuController =
        GuideMenuController(
            actions = ChannelActions(dao, CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))),
            zapAway = { zapped += it.id },
            focusedRow = { row },
            info = { infoData },
            callbacks =
                GuideCallbacks(
                    onFullscreen = {},
                    onOpenSearch = { searches += 1 },
                    onOpenSettings = { settingsOpens += 1 },
                ),
        )

    private fun TestScope.buildOpenSheet(): GuideMenuController = build().apply { openRowMenu() }

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
    fun `premium-locked reference rows open the paywall and back pops to the sheet`() {
        runTest {
            val premium =
                listOf(
                    PlayerMenuItem.OPEN_IN_EXTERNAL_PLAYER,
                    PlayerMenuItem.RECORD,
                    PlayerMenuItem.CUSTOM_RECORDING,
                    PlayerMenuItem.ADD_TO_MY_LIST,
                    PlayerMenuItem.BLOCK_CHANNEL,
                    PlayerMenuItem.MANAGE_FAVORITES,
                    PlayerMenuItem.REORDER_CHANNELS,
                )
            val menu = buildOpenSheet()

            premium.forEach { item ->
                menu.onMenuItem(item)
                assertEquals(GuideLayer.Paywall(item.label, back = GuideLayer.RowMenu), menu.layer.value)
                menu.close()
                assertEquals(GuideLayer.RowMenu, menu.layer.value)
            }
        }
    }

    @Test
    fun `rows without captured behavior land on the branded coming-soon`() {
        runTest {
            val unknown =
                listOf(
                    PlayerMenuItem.ASSIGN_EPG,
                    PlayerMenuItem.MANAGE_BLOCKING,
                    PlayerMenuItem.MANAGE_VISIBILITY,
                    PlayerMenuItem.COPY_CHANNELS,
                    PlayerMenuItem.CREATE_GROUP,
                    PlayerMenuItem.GROUP_OPTIONS,
                )
            val menu = buildOpenSheet()

            unknown.forEach { item ->
                menu.onMenuItem(item)
                assertEquals(GuideLayer.ComingSoon(item.label), menu.layer.value)
                menu.close()
                assertEquals(GuideLayer.RowMenu, menu.layer.value)
            }
        }
    }

    @Test
    fun `channel options pushes the pane and back pops one level at a time`() {
        runTest {
            val menu = buildOpenSheet()

            menu.onMenuItem(PlayerMenuItem.CHANNEL_OPTIONS)
            assertEquals(GuideLayer.ChannelOptions("News One"), menu.layer.value)

            menu.close()
            assertEquals(GuideLayer.RowMenu, menu.layer.value)
            menu.close()
            assertEquals(GuideLayer.Grid, menu.layer.value)
        }
    }

    @Test
    fun `unlock premium is the only live row of the channel options pane`() {
        runTest {
            val menu = buildOpenSheet()
            menu.onMenuItem(PlayerMenuItem.CHANNEL_OPTIONS)

            menu.onChannelOption("channel_options.name")
            assertEquals(GuideLayer.ChannelOptions("News One"), menu.layer.value)

            menu.onChannelOption(RowIds.UNLOCK_PREMIUM)
            assertEquals(
                GuideLayer.Paywall("Channel options", back = GuideLayer.ChannelOptions("News One")),
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

            menu.onMenuItem(PlayerMenuItem.CHANNEL_OPTIONS)
            menu.close()

            assertEquals(GuideLayer.RowMenu, menu.layer.value)
            assertEquals(PlayerMenuItem.CHANNEL_OPTIONS, menu.sheetFocus.restore)
        }
    }

    @Test
    fun `rows that leave the sheet clear the refocus memory`() {
        runTest {
            val menu = buildOpenSheet()
            menu.onMenuItem(PlayerMenuItem.RECORD)
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
    fun `cell dropdown rows still paywall back to the grid`() {
        runTest {
            val menu = build()

            menu.onCellAction(GuideCellAction.REMIND)
            assertEquals(GuideLayer.Paywall("Remind"), menu.layer.value)

            menu.close()
            assertEquals(GuideLayer.Grid, menu.layer.value)
        }
    }
}
