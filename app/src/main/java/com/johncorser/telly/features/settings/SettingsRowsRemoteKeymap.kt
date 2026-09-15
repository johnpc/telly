package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings

/**
 * Remote control sub-panes (TiviMate's premium key-remap screens, ux-spec
 * §2.13.6 — exact option lists uncaptured, so telly offers only remaps the
 * slices can honor). Summaries show the persisted raw picker labels;
 * defaults render the device-verified key map.
 */
fun remoteTvGuideRows(s: SettingsRepository): List<SettingsRow> =
    listOf(
        SettingsRow.Value(
            id = RowIds.REMOTE_GUIDE_LEFT_RIGHT,
            title = "Left/Right buttons",
            summary = s.get(TellySettings.REMOTE_GUIDE_LEFT_RIGHT),
        ),
        SettingsRow.Value(
            id = RowIds.REMOTE_GUIDE_CHANNEL_UP_DOWN,
            title = "Channel up/down buttons",
            summary = s.get(TellySettings.REMOTE_GUIDE_CHANNEL_UP_DOWN),
        ),
        SettingsRow.Value(
            id = RowIds.REMOTE_GUIDE_LONG_OK,
            title = "Long press OK",
            summary = s.get(TellySettings.REMOTE_GUIDE_LONG_OK),
        ),
    )

/** The Player sub-pane: bare fullscreen-playback key remaps. */
fun remotePlayerRows(s: SettingsRepository): List<SettingsRow> =
    listOf(
        SettingsRow.Value(
            id = RowIds.REMOTE_PLAYER_OK,
            title = "OK button",
            summary = s.get(TellySettings.REMOTE_PLAYER_OK),
        ),
        SettingsRow.Value(
            id = RowIds.REMOTE_PLAYER_UP_DOWN,
            title = "Up/Down buttons",
            summary = s.get(TellySettings.REMOTE_PLAYER_UP_DOWN),
        ),
        SettingsRow.Value(
            id = RowIds.REMOTE_PLAYER_LEFT_RIGHT,
            title = "Left/Right buttons",
            summary = s.get(TellySettings.REMOTE_PLAYER_LEFT_RIGHT),
        ),
        SettingsRow.Value(
            id = RowIds.REMOTE_PLAYER_LONG_OK,
            title = "Long press OK",
            summary = s.get(TellySettings.REMOTE_PLAYER_LONG_OK),
        ),
    )
