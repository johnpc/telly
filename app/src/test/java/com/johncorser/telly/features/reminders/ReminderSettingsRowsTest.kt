package com.johncorser.telly.features.reminders

import com.johncorser.telly.core.settings.InMemoryKeyValueStore
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.settings.RowIds
import com.johncorser.telly.features.settings.SettingsRow
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderSettingsRowsTest {
    private val s = SettingsRepository(InMemoryKeyValueStore())
    private val item =
        ReminderListItem(id = 3, title = "Morning Report", channelName = "News One", airTime = "Sun, Sep 13, 2:45 PM")

    @Test
    fun `the lead row shows the captured default of five minutes`() {
        val lead = remindersRows(s, emptyList()).first() as SettingsRow.Value
        assertEquals(RowIds.REMINDERS_LEAD, lead.id)
        assertEquals("Show reminder before, min", lead.title)
        assertEquals("5", lead.summary)
    }

    @Test
    fun `the lead row reflects the stored value`() {
        s.set(TellySettings.REMINDER_LEAD_MINUTES, 30)
        assertEquals("30", (remindersRows(s, emptyList()).first() as SettingsRow.Value).summary)
    }

    @Test
    fun `without reminders the pane shows the empty note`() {
        val rows = remindersRows(s, emptyList())
        assertEquals(SettingsRow.Note("No reminders", accent = false), rows[1])
        assertEquals(2, rows.size)
    }

    @Test
    fun `scheduled reminders list under a header with channel and air time`() {
        val rows = remindersRows(s, listOf(item))
        assertEquals(SettingsRow.Header("Scheduled reminders"), rows[1])
        val row = rows[2] as SettingsRow.Value
        assertEquals(RowIds.REMINDER_PREFIX + "3", row.id)
        assertEquals("Morning Report", row.title)
        assertEquals("News One • Sun, Sep 13, 2:45 PM", row.summary)
    }
}
