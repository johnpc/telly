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
