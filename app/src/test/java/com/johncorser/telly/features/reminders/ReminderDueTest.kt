package com.johncorser.telly.features.reminders

import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderDueTest {
    private val lead = 5 * ReminderDue.MINUTE_MS
    private val reminder = testReminder(id = 1, startMs = 100 * ReminderDue.MINUTE_MS)

    private fun stateAt(nowMs: Long) = ReminderDue.stateOf(reminder, nowMs, lead)

    @Test
    fun `pending strictly before start minus lead`() {
        assertEquals(ReminderDue.State.PENDING, stateAt(reminder.startMs - lead - 1))
    }

    @Test
    fun `due from start minus lead through the programme's run`() {
        assertEquals(ReminderDue.State.DUE, stateAt(reminder.startMs - lead))
        assertEquals(ReminderDue.State.DUE, stateAt(reminder.startMs))
        assertEquals(ReminderDue.State.DUE, stateAt(reminder.stopMs - 1))
    }

    @Test
    fun `expired once the programme ended`() {
        assertEquals(ReminderDue.State.EXPIRED, stateAt(reminder.stopMs))
        assertEquals(ReminderDue.State.EXPIRED, stateAt(reminder.stopMs + lead))
    }
}
