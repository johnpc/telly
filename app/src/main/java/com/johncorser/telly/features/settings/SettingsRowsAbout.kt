package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings

/** Other pane rows (catalogue 68): Search, Reminders, Recording and VOD are all live. */
fun otherRows(): List<SettingsRow> =
    listOf(
        SettingsRow.Value(id = RowIds.OTHER_SEARCH, title = "Search"),
        SettingsRow.Value(id = RowIds.OTHER_REMINDERS, title = "Reminders"),
        SettingsRow.Value(id = RowIds.OTHER_RECORDING, title = "Recording"),
        SettingsRow.Value(id = RowIds.OTHER_VOD, title = "VOD"),
    )

/** About pane rows (catalogue 53). */
fun aboutRows(
    s: SettingsRepository,
    versionName: String,
): List<SettingsRow> =
    listOf(
        SettingsRow.Toggle(
            id = RowIds.ABOUT_STATISTICS,
            title = "Send anonymous statistics to improve the app",
            checked = s.get(TellySettings.SEND_STATISTICS),
        ),
        SettingsRow.Action(id = RowIds.ABOUT_PRIVACY_POLICY, title = "Privacy policy"),
        SettingsRow.Value(id = RowIds.ABOUT_VERSION, title = "Version", summary = versionName),
    )
