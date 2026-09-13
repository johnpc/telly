package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings

/**
 * Appearance pane rows (catalogue 59). The sub-screens and Language/Font
 * size have no telly behavior yet, so they render premium-locked exactly
 * like the reference; Color theme is the live accent picker.
 */
fun appearanceRows(s: SettingsRepository): List<SettingsRow> =
    panePrelude() +
        listOf(
            SettingsRow.Value(id = RowIds.APPEARANCE_TV_GUIDE, title = "TV guide", locked = true),
            SettingsRow.Value(id = RowIds.APPEARANCE_PLAYER, title = "Player", locked = true),
            SettingsRow.Value(id = RowIds.APPEARANCE_GROUPS, title = "Groups", locked = true),
            SettingsRow.Value(id = RowIds.APPEARANCE_LOGOS, title = "Logos", locked = true),
            SettingsRow.Value(
                id = RowIds.APPEARANCE_LANGUAGE,
                title = "Language",
                summary = s.get(TellySettings.LANGUAGE),
                locked = true,
            ),
            SettingsRow.Value(
                id = RowIds.APPEARANCE_FONT_SIZE,
                title = "Font size",
                summary = s.get(TellySettings.FONT_SIZE),
                locked = true,
            ),
            SettingsRow.Value(
                id = RowIds.APPEARANCE_COLOR_THEME,
                title = "Color theme",
                summary = SettingsPickers.themeLabel(s.get(TellySettings.ACCENT_COLOR)),
            ),
        )
