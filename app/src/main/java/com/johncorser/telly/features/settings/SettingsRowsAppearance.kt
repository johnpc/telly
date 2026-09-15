package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings

/**
 * Appearance pane rows (catalogue 59), all live: the four sub-screens push
 * their panes, Language/Font size open pickers, Color theme is the accent
 * picker. telly has no premium tier, so nothing here stays locked.
 */
fun appearanceRows(s: SettingsRepository): List<SettingsRow> =
    listOf(
        SettingsRow.Value(id = RowIds.APPEARANCE_TV_GUIDE, title = "TV guide"),
        SettingsRow.Value(id = RowIds.APPEARANCE_PLAYER, title = "Player"),
        SettingsRow.Value(id = RowIds.APPEARANCE_GROUPS, title = "Groups"),
        SettingsRow.Value(id = RowIds.APPEARANCE_LOGOS, title = "Logos"),
        SettingsRow.Value(
            id = RowIds.APPEARANCE_LANGUAGE,
            title = "Language",
            summary = s.get(TellySettings.LANGUAGE),
        ),
        SettingsRow.Value(
            id = RowIds.APPEARANCE_FONT_SIZE,
            title = "Font size",
            summary = s.get(TellySettings.FONT_SIZE),
        ),
        SettingsRow.Value(
            id = RowIds.APPEARANCE_COLOR_THEME,
            title = "Color theme",
            summary = SettingsPickers.themeLabel(s.get(TellySettings.ACCENT_COLOR)),
        ),
    )
