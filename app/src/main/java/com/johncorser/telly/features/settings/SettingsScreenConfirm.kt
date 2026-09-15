package com.johncorser.telly.features.settings

import androidx.compose.runtime.Composable
import com.johncorser.telly.R

/**
 * "Delete playlist?" GuidedStep confirm, verbatim from the live capture
 * (settings-round1/ref/08): warning icon, question title, channel-loss
 * body, actions Delete / Cancel.
 */
@Composable
internal fun SettingsScreenConfirmDelete(
    model: SettingsViewModel,
    confirm: SettingsOverlay.ConfirmDelete,
) {
    SettingsScreenGuidedStep(
        iconRes = R.drawable.ic_settings_warning,
        title = "Delete playlist?",
        bodyLines = listOf("All channels from the playlist \"${confirm.name}\" will no longer be available"),
        actions =
            listOf(
                "Delete" to { model.confirmDelete() },
                "Cancel" to { model.dismissOverlay() },
            ),
    )
}

/**
 * "Clear search history?" confirm (Other -> Search): uncapturable (the
 * reference locks the Search pane behind premium), shaped after the
 * captured delete-playlist GuidedStep above.
 */
@Composable
internal fun SettingsScreenConfirmClearHistory(model: SettingsViewModel) {
    SettingsScreenGuidedStep(
        iconRes = R.drawable.ic_settings_warning,
        title = "Clear search history?",
        bodyLines = listOf("All saved search queries will be removed"),
        actions =
            listOf(
                "Clear" to { model.confirmClearHistory() },
                "Cancel" to { model.dismissOverlay() },
            ),
    )
}

/**
 * "Delete EPG source?" confirm: uncapturable (the reference locks source
 * management behind premium), shaped after the captured delete-playlist
 * GuidedStep above.
 */
@Composable
internal fun SettingsScreenConfirmDeleteSource(
    model: SettingsViewModel,
    confirm: SettingsOverlay.ConfirmDeleteSource,
) {
    SettingsScreenGuidedStep(
        iconRes = R.drawable.ic_settings_warning,
        title = "Delete EPG source?",
        bodyLines = listOf("Guide data from \"${confirm.name}\" will no longer be updated"),
        actions =
            listOf(
                "Delete" to { model.confirmDeleteEpgSource() },
                "Cancel" to { model.dismissOverlay() },
            ),
    )
}
