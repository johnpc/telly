package com.johncorser.telly.features.reminders

import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.settings.RowIds
import com.johncorser.telly.features.settings.SettingsRow

/**
 * Settings -> Other -> Reminders pane rows: the lead-time picker row plus
 * the scheduled reminders (title over channel + air time), or the
 * "No reminders" note while nothing is scheduled. Not capturable (the
 * reference locks the pane behind premium); shaped after the EPG pane idiom.
 */
fun remindersRows(
    s: SettingsRepository,
    reminders: List<ReminderListItem>,
): List<SettingsRow> {
    val lead =
        SettingsRow.Value(
            id = RowIds.REMINDERS_LEAD,
            title = "Show reminder before, min",
            summary = s.get(TellySettings.REMINDER_LEAD_MINUTES).toString(),
        )
    if (reminders.isEmpty()) return listOf(lead, SettingsRow.Note("No reminders", accent = false))
    return listOf(lead, SettingsRow.Header("Scheduled reminders")) +
        reminders.map { item ->
            SettingsRow.Value(
                id = RowIds.REMINDER_PREFIX + item.id,
                title = item.title,
                summary = "${item.channelName} • ${item.airTime}",
            )
        }
}
