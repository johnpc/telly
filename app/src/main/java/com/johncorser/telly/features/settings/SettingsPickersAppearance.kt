package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.AppLanguage
import com.johncorser.telly.core.settings.TellySettings

/**
 * The Appearance sub-pane pickers, concatenated into [SettingsPickers].
 * Option lists follow the ux-spec where stated (font size, language) and
 * the most TiviMate-plausible minimal set otherwise; each stored default
 * (TellySettings) reproduces telly's current rendering exactly.
 */
private fun labelled(labels: List<String>) = labels.map { PickerOption(it, it) }

internal val appearancePickerSpecs: List<PickerSpec> =
    listOf(
        PickerSpec(
            rowId = RowIds.APPEARANCE_GUIDE_VISIBLE_CHANNELS,
            title = "Number of visible channels",
            key = TellySettings.GUIDE_VISIBLE_CHANNELS.key,
            options = listOf(6, 7, 8, 9).map { PickerOption(it.toString(), it.toString()) },
        ),
        PickerSpec(
            rowId = RowIds.APPEARANCE_GUIDE_TRANSPARENCY,
            title = "Panel transparency",
            key = TellySettings.GUIDE_TRANSPARENCY.key,
            options = labelled(listOf("Opaque", "90%", "80%", "70%")),
        ),
        PickerSpec(
            rowId = RowIds.APPEARANCE_PLAYER_TRANSPARENCY,
            title = "Panels transparency",
            key = TellySettings.PLAYER_TRANSPARENCY.key,
            options = listOf(0, 10, 25, 50).map { PickerOption("$it%", it.toString()) },
        ),
        PickerSpec(
            rowId = RowIds.APPEARANCE_PLAYER_TIMEOUT,
            title = "Panels timeout, sec",
            key = TellySettings.PLAYER_PANEL_TIMEOUT_SEC.key,
            options = listOf(2, 5, 8, 10).map { PickerOption(it.toString(), it.toString()) },
        ),
        PickerSpec(
            rowId = RowIds.APPEARANCE_LOGOS_BACKGROUND,
            title = "Logo background",
            key = TellySettings.LOGO_BACKGROUND.key,
            options = labelled(listOf("Default", "Transparent", "Dark", "Light")),
        ),
        PickerSpec(
            rowId = RowIds.APPEARANCE_LANGUAGE,
            title = "Language",
            key = TellySettings.LANGUAGE.key,
            options = labelled(AppLanguage.OPTIONS),
        ),
        PickerSpec(
            rowId = RowIds.APPEARANCE_FONT_SIZE,
            title = "Font size",
            key = TellySettings.FONT_SIZE.key,
            options = labelled(listOf("Small", "Medium", "Large", "Huge")),
        ),
    )

internal val appearancePickerDefaults: Map<String, String> =
    mapOf(
        TellySettings.GUIDE_VISIBLE_CHANNELS.key to TellySettings.GUIDE_VISIBLE_CHANNELS.default.toString(),
        TellySettings.GUIDE_TRANSPARENCY.key to TellySettings.GUIDE_TRANSPARENCY.default,
        TellySettings.PLAYER_TRANSPARENCY.key to TellySettings.PLAYER_TRANSPARENCY.default.toString(),
        TellySettings.PLAYER_PANEL_TIMEOUT_SEC.key to TellySettings.PLAYER_PANEL_TIMEOUT_SEC.default.toString(),
        TellySettings.LOGO_BACKGROUND.key to TellySettings.LOGO_BACKGROUND.default,
        TellySettings.LANGUAGE.key to TellySettings.LANGUAGE.default,
        TellySettings.FONT_SIZE.key to TellySettings.FONT_SIZE.default,
    )
