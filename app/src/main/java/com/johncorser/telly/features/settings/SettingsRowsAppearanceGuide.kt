package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings

/**
 * Appearance -> TV guide and Appearance -> Player sub-panes (ux-spec §3
 * Appearance: "TV Guide" rows/density, "Player" overlay elements plus the
 * premium panel-transparency/timeout options). Defaults render telly's
 * current guide and overlay pixels exactly.
 */
fun appearanceTvGuideRows(s: SettingsRepository): List<SettingsRow> =
    listOf(
        SettingsRow.Value(
            id = RowIds.APPEARANCE_GUIDE_VISIBLE_CHANNELS,
            title = "Number of visible channels",
            summary = s.get(TellySettings.GUIDE_VISIBLE_CHANNELS).toString(),
        ),
        SettingsRow.Value(
            id = RowIds.APPEARANCE_GUIDE_TRANSPARENCY,
            title = "Panel transparency",
            summary = s.get(TellySettings.GUIDE_TRANSPARENCY),
        ),
        SettingsRow.Toggle(
            id = RowIds.APPEARANCE_GUIDE_CHANNEL_NUMBERS,
            title = "Show channel numbers",
            checked = s.get(TellySettings.SHOW_CHANNEL_NUMBERS),
        ),
    )

fun appearancePlayerRows(s: SettingsRepository): List<SettingsRow> =
    listOf(
        SettingsRow.Value(
            id = RowIds.APPEARANCE_PLAYER_TRANSPARENCY,
            title = "Panels transparency",
            summary = "${s.get(TellySettings.PLAYER_TRANSPARENCY)}%",
        ),
        SettingsRow.Value(
            id = RowIds.APPEARANCE_PLAYER_TIMEOUT,
            title = "Panels timeout, sec",
            summary = s.get(TellySettings.PLAYER_PANEL_TIMEOUT_SEC).toString(),
        ),
        SettingsRow.Toggle(
            id = RowIds.APPEARANCE_PLAYER_SHOW_CLOCK,
            title = "Show clock",
            checked = s.get(TellySettings.PLAYER_SHOW_CLOCK),
        ),
    )
