package com.johncorser.telly.features.reminders

import com.johncorser.telly.features.settings.RowIds
import com.johncorser.telly.features.settings.SettingsOverlay
import com.johncorser.telly.features.settings.SettingsViewModel
import com.johncorser.telly.features.settings.launch
import com.johncorser.telly.features.settings.showOverlay

/** OK on a scheduled reminder opens the delete GuidedStep confirm. */
internal fun SettingsViewModel.confirmDeleteReminderOverlay(rowId: String) {
    val id = rowId.removePrefix(RowIds.REMINDER_PREFIX).toLongOrNull() ?: return
    val item = reminderItems.value.firstOrNull { it.id == id } ?: return
    showOverlay(SettingsOverlay.ConfirmDeleteReminder(reminderId = id, title = item.title))
}

/** OK on Delete in the confirm removes the reminder and closes the step. */
fun SettingsViewModel.confirmDeleteReminder() {
    val confirm = state.value.overlay as? SettingsOverlay.ConfirmDeleteReminder ?: return
    launch { reminders?.delete(confirm.reminderId) }
    dismissOverlay()
}
