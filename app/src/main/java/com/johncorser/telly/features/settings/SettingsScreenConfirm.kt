package com.johncorser.telly.features.settings

import androidx.compose.runtime.Composable
import com.johncorser.telly.R

/**
 * "Delete playlist" GuidedStep confirm, verbatim from capture 22:
 * guidance "Delete playlist" + the playlist name, actions Delete / Cancel.
 */
@Composable
internal fun SettingsScreenConfirmDelete(
    model: SettingsViewModel,
    confirm: SettingsOverlay.ConfirmDelete,
) {
    SettingsScreenGuidedStep(
        iconRes = R.drawable.ic_wizard_playlist_add,
        title = "Delete playlist",
        bodyLines = listOf(confirm.name),
        actions =
            listOf(
                "Delete" to { model.confirmDelete() },
                "Cancel" to { model.dismissOverlay() },
            ),
    )
}

/**
 * The Unlock Premium paywall, verbatim from capture 28. Shared with the
 * guide slice, whose future-cell dropdown rows are all premium-gated.
 */
@Composable
internal fun SettingsScreenPaywall(onClose: () -> Unit) {
    SettingsScreenGuidedStep(
        iconRes = R.drawable.ic_settings_lock,
        title = "Unlock Premium",
        bodyLines =
            listOf(
                "You will get access to:",
                "• Support for multiple playlists",
                "• Favorites management",
                "• Catch-up",
                "• Customizable EPG update intervals",
                "• Customizable panels transparency and timeout",
                "• Manual channels sorting",
                "• Turning on last channel on app start",
                "• Auto frame rate (AFR)",
                "• and much more",
            ),
        actions =
            listOf(
                "Next" to onClose,
                "Cancel" to onClose,
            ),
    )
}
