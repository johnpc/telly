package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings

/**
 * Parental controls pane rows (catalogue 67/69). The master toggle's title
 * IS its state ("Off"/"On") in the capture. The "Require PIN for" list
 * continues below the fold in the reference (unscrollable in free tier);
 * additional lockable surfaces land with the slices that own them.
 */
fun parentalRows(s: SettingsRepository): List<SettingsRow> {
    val enabled = s.get(TellySettings.PARENTAL_ENABLED)
    return panePrelude() +
        listOf(
            SettingsRow.Toggle(
                id = RowIds.PARENTAL_MASTER,
                title = if (enabled) "On" else "Off",
                checked = enabled,
            ),
            SettingsRow.Action(id = RowIds.PARENTAL_CHANGE_PIN, title = "Change PIN"),
            SettingsRow.Value(
                id = RowIds.PARENTAL_PIN_INPUT,
                title = "PIN input method",
                summary = s.get(TellySettings.PARENTAL_PIN_INPUT_METHOD),
            ),
            SettingsRow.Value(
                id = RowIds.PARENTAL_RELOCK,
                title = "Don't require PIN after unlocking",
                summary = s.get(TellySettings.PARENTAL_RELOCK),
            ),
            SettingsRow.Toggle(
                id = RowIds.PARENTAL_CHANNELS_ONLY,
                title = "Don't require for channels only",
                checked = s.get(TellySettings.PARENTAL_CHANNELS_ONLY),
            ),
            SettingsRow.Header("Require PIN for"),
            SettingsRow.Toggle(
                id = RowIds.PARENTAL_REQUIRE_SETTINGS,
                title = "Settings",
                checked = s.get(TellySettings.PARENTAL_REQUIRE_FOR_SETTINGS),
            ),
            SettingsRow.Toggle(
                id = RowIds.PARENTAL_REQUIRE_PLAYLISTS,
                title = "Settings | Playlists",
                checked = s.get(TellySettings.PARENTAL_REQUIRE_FOR_PLAYLISTS),
            ),
        )
}
