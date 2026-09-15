package com.johncorser.telly.features.reminders

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

@OptIn(ExperimentalCoroutinesApi::class)
class GuideRemindersTest {
    private val dao = FakeReminderDao()
    private val store = ReminderStore(dao)
    private val channel = testChannel(7, 1, "News One")
    private val program = testProgram("tvg-7", 10_000L, 40_000L, "Morning Report")

    private fun TestScope.build() = GuideReminders(store, CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

    @Test
    fun `toggle round-trips through the store and the keys follow`() =
        runTest {
            val reminders = build()
            assertTrue(reminders.keys.value.isEmpty())

            reminders.toggle(channel, program)
            assertEquals(setOf(ReminderKey(7L, 10_000L, "Morning Report")), reminders.keys.value)

            reminders.toggle(channel, program)
            assertTrue(reminders.keys.value.isEmpty())
        }

    @Test
    fun `labelFor relabels only the exact programme with a reminder`() {
        val keys = setOf(ReminderKey(7L, 10_000L, "Morning Report"))
        assertEquals(GuideReminders.UNSET_LABEL, GuideReminders.labelFor(keys, 7L, program))
        assertEquals(GuideReminders.SET_LABEL, GuideReminders.labelFor(keys, 8L, program))
        assertEquals(
            GuideReminders.SET_LABEL,
            GuideReminders.labelFor(keys, 7L, testProgram("tvg-7", 50_000L, 60_000L, "Morning Report")),
        )
    }

    @Test
    fun `labelFor stays Remind for filler cells and empty keys`() {
        assertEquals(GuideReminders.SET_LABEL, GuideReminders.labelFor(emptySet(), 7L, program))
        assertEquals(GuideReminders.SET_LABEL, GuideReminders.labelFor(emptySet(), 7L, null))
        assertEquals(GuideReminders.SET_LABEL, GuideReminders.labelFor(emptySet(), null, program))
    }
}
