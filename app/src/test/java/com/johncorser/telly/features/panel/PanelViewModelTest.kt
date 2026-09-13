package com.johncorser.telly.features.panel

import com.johncorser.telly.testutil.FakeChannelDao
import com.johncorser.telly.testutil.FakeProgramDao
import com.johncorser.telly.testutil.asFavorite
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
import org.junit.Test
import java.util.GregorianCalendar
import java.util.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class PanelViewModelTest {
    private val utc = TimeZone.getTimeZone("UTC")
    private val channels =
        listOf(
            testChannel(1, 1, "News One", group = "News"),
            testChannel(2, 2, "News Two", group = "News"),
            testChannel(3, 3, "Sports Arena", group = "Sports").asFavorite(),
            testChannel(4, 4, "Sports Extra", group = "Sports"),
            testChannel(5, 5, "Music Box", group = "Music"),
        )
    private val dao = FakeChannelDao(channels)
    private val programs = FakeProgramDao()

    private fun at(
        hour: Int,
        minute: Int,
    ): Long =
        GregorianCalendar(utc)
            .apply {
                set(2026, 8, 13, hour, minute, 0)
                set(GregorianCalendar.MILLISECOND, 0)
            }.timeInMillis

    private fun TestScope.buildPanel(clock: () -> Long = { at(14, 45) }): PanelViewModel =
        PanelViewModel(
            channelDao = dao,
            epgRepository = testEpgRepository(programs),
            clock = clock,
            scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler)),
            zone = utc,
        )

    @Test
    fun `groups start with favorites and all channels then playlist order`() =
        runTest {
            val panel = buildPanel()

            assertEquals(
                listOf("Favorites", "All channels", "News", "Sports", "Music"),
                panel.groups.value,
            )
        }

    @Test
    fun `all-channels rows keep playlist numbers and now-next data`() =
        runTest {
            programs.programs.value =
                listOf(
                    testProgram("tvg-1", at(14, 30), at(15, 45), "Business Hour", episode = "S1 E7")
                        .describedAs("All-new episode."),
                    testProgram("tvg-1", at(15, 45), at(17, 15), "Newsroom Live"),
                )
            val panel = buildPanel()

            val rows = panel.rows.value

            assertEquals(listOf(1, 2, 3, 4, 5), rows.map { it.displayNumber })
            val first = rows.first()
            assertEquals("Business Hour. S1 E7", first.nowTitle)
            assertEquals("02:30 — 03:45 PM", first.nowRange)
            assertEquals("60 min", first.remaining)
            assertEquals("All-new episode.", first.description)
            assertEquals("Newsroom Live", first.nextTitle)
            assertEquals(200, first.progressPermille)
        }

    @Test
    fun `group rows renumber from one like capture 74`() =
        runTest {
            val panel = buildPanel()

            panel.selectGroup("Sports")

            assertEquals(listOf(3L, 4L), panel.rows.value.map { it.channel.id })
            assertEquals(listOf(1, 2), panel.rows.value.map { it.displayNumber })
        }

    @Test
    fun `the favorites group shows only favorite channels`() =
        runTest {
            val panel = buildPanel()

            panel.selectGroup(PanelViewModel.FAVORITES)

            assertEquals(listOf(3L), panel.rows.value.map { it.channel.id })
        }

    @Test
    fun `opening focused on a channel of the selected group moves focus there`() =
        runTest {
            val panel = buildPanel()

            panel.openFocusedOn(2)

            assertEquals(1, panel.focusIndex.value)
            assertEquals(PanelViewModel.ALL_CHANNELS, panel.selectedGroup.value)
        }

    @Test
    fun `opening focused on a channel outside the group falls back to all channels`() =
        runTest {
            val panel = buildPanel()
            panel.selectGroup("Sports")

            panel.openFocusedOn(1)

            assertEquals(PanelViewModel.ALL_CHANNELS, panel.selectedGroup.value)
            assertEquals(0, panel.focusIndex.value)
        }

    @Test
    fun `focus position is remembered per group`() =
        runTest {
            val panel = buildPanel()
            panel.onRowFocused(2)

            panel.selectGroup("Sports")
            assertEquals(0, panel.focusIndex.value)
            panel.onRowFocused(1)

            panel.selectGroup(PanelViewModel.ALL_CHANNELS)
            assertEquals(2, panel.focusIndex.value)
            panel.selectGroup("Sports")
            assertEquals(1, panel.focusIndex.value)
        }

    @Test
    fun `reselecting the current group keeps the focus position`() =
        runTest {
            val panel = buildPanel()
            panel.onRowFocused(3)

            panel.selectGroup(PanelViewModel.ALL_CHANNELS)

            assertEquals(3, panel.focusIndex.value)
        }

    @Test
    fun `the clock refreshes each time the panel opens`() =
        runTest {
            var now = at(14, 45)
            val panel = buildPanel { now }
            assertEquals("Sun, Sep 13, 2:45 PM", panel.clockText.value)

            now = at(15, 2)
            panel.openFocusedOn(null)

            assertEquals("Sun, Sep 13, 3:02 PM", panel.clockText.value)
        }
}
