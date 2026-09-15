package com.johncorser.telly.features.guide

import com.johncorser.telly.features.playback.ChannelActions
import com.johncorser.telly.features.reminders.FakeReminderDao
import com.johncorser.telly.features.reminders.GuideReminders
import com.johncorser.telly.features.reminders.ReminderStore
import com.johncorser.telly.testutil.FakeChannelDao
import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.testProgram
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The cell dropdown's live Remind row (set / relabel / remove). */
@OptIn(ExperimentalCoroutinesApi::class)
class GuideMenuRemindTest {
    private val channelDao = FakeChannelDao(listOf(testChannel(1, 1, "News One")))
    private val program = testProgram("tvg-1", 10_000L, 40_000L, "Morning Report")
    private val cell = GuideCell(program.startMs, program.endMs, program)
    private val fillerCell = GuideCell(40_000L, 70_000L, program = null)
    private val reminderDao = FakeReminderDao()
    private val store = ReminderStore(reminderDao)
    private val row = GuideRow(channelDao.channels.value.first(), 1, listOf(cell))

    private fun TestScope.build(): GuideMenuController {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        return GuideMenuController(
            actions = ChannelActions(channelDao, scope),
            zapAway = {},
            focusedRow = { row },
            info = { null },
            callbacks = GuideCallbacks(onFullscreen = {}, onOpenSearch = {}, onOpenSettings = {}),
        ).apply { remind.reminders = GuideReminders(store, scope) }
    }

    @Test
    fun `remind on a future cell creates the reminder and returns to the grid`() {
        runTest {
            val menu = build()
            menu.show(GuideLayer.CellMenu(cell))

            menu.onCellAction(GuideCellAction.REMIND)

            assertEquals(GuideLayer.Grid, menu.layer.value)
            assertEquals("Morning Report", reminderDao.rows.value.single().title)
        }
    }

    @Test
    fun `remind on a programme with a reminder removes it`() {
        runTest {
            val menu = build()
            menu.show(GuideLayer.CellMenu(cell))
            menu.onCellAction(GuideCellAction.REMIND)

            menu.show(GuideLayer.CellMenu(cell))
            menu.onCellAction(GuideCellAction.REMIND)

            assertTrue(reminderDao.rows.value.isEmpty())
        }
    }

    @Test
    fun `the remind label follows the reminder's existence`() {
        runTest {
            val menu = build()
            menu.show(GuideLayer.CellMenu(cell))
            assertEquals("Remind", menu.remind.label(GuideCellAction.REMIND, menu.remind.keys.value))
            assertEquals("Record", menu.remind.label(GuideCellAction.RECORD, menu.remind.keys.value))

            menu.onCellAction(GuideCellAction.REMIND)
            menu.show(GuideLayer.CellMenu(cell))

            assertEquals("Remove reminder", menu.remind.label(GuideCellAction.REMIND, menu.remind.keys.value))
        }
    }

    @Test
    fun `remind on a no-information filler cell falls back to coming-soon`() {
        runTest {
            val menu = build()
            menu.show(GuideLayer.CellMenu(fillerCell))

            menu.onCellAction(GuideCellAction.REMIND)

            assertEquals(GuideLayer.ComingSoon("Remind"), menu.layer.value)
            assertTrue(reminderDao.rows.value.isEmpty())
        }
    }
}
