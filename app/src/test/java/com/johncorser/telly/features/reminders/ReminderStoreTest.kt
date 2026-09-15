package com.johncorser.telly.features.reminders

import com.johncorser.telly.testutil.testProgram
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderStoreTest {
    private val dao = FakeReminderDao()
    private val store = ReminderStore(dao)
    private val program = testProgram("tvg-1", 10_000L, 40_000L, "Morning Report")

    @Test
    fun `toggle creates a reminder carrying the programme's identity`() =
        runTest {
            store.toggle(7L, program)

            val row = store.reminders.first().single()
            assertEquals(7L, row.channelId)
            assertEquals("Morning Report", row.title)
            assertEquals(10_000L, row.startMs)
            assertEquals(40_000L, row.stopMs)
        }

    @Test
    fun `toggling the same programme again removes the reminder`() =
        runTest {
            store.toggle(7L, program)
            store.toggle(7L, program)

            assertTrue(store.reminders.first().isEmpty())
        }

    @Test
    fun `toggles on different channels or programmes are independent`() =
        runTest {
            store.toggle(7L, program)
            store.toggle(8L, program)
            store.toggle(7L, testProgram("tvg-1", 50_000L, 60_000L, "World News Now"))

            assertEquals(3, store.reminders.first().size)
        }

    @Test
    fun `remove deletes by row id`() =
        runTest {
            store.toggle(7L, program)
            store.remove(store.reminders.first().single().id)

            assertTrue(store.reminders.first().isEmpty())
        }
}
