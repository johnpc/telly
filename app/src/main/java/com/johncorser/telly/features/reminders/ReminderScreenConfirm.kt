package com.johncorser.telly.features.reminders

import androidx.compose.runtime.Composable
import com.johncorser.telly.R
import com.johncorser.telly.features.settings.SettingsOverlay
import com.johncorser.telly.features.settings.SettingsScreenGuidedStep
import com.johncorser.telly.features.settings.SettingsViewModel
import com.johncorser.telly.features.settings.dismissOverlay

/**
 * "Delete reminder?" GuidedStep confirm (uncapturable — the reference locks
 * reminders behind premium), shaped after the captured delete-playlist
 * confirm.
 */
@Composable
internal fun ReminderScreenConfirmDelete(
    model: SettingsViewModel,
    confirm: SettingsOverlay.ConfirmDeleteReminder,
) {
    SettingsScreenGuidedStep(
        iconRes = R.drawable.ic_settings_warning,
        title = "Delete reminder?",
        bodyLines = listOf("You will no longer be reminded about \"${confirm.title}\""),
        actions =
            listOf(
                "Delete" to { model.confirmDeleteReminder() },
                "Cancel" to { model.dismissOverlay() },
            ),
    )
}
