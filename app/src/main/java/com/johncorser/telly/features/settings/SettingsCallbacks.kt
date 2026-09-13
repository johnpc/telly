package com.johncorser.telly.features.settings

/**
 * The view model's exits into the shell: routing into the add-playlist
 * wizard and the SAF document flows (whose intent wiring lives in the thin
 * SettingsScreen layer).
 */
class SettingsCallbacks(
    val onAddPlaylist: () -> Unit = {},
    val onExportBackup: (json: String) -> Unit = {},
    /** Mutable: the SAF launcher only exists once the screen is composed. */
    var onImportBackup: () -> Unit = {},
    val onOpenPrivacyPolicy: () -> Unit = {},
)
