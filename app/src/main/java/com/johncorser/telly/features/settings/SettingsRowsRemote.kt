package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings

/** Remote control pane rows (catalogue 64–65). */
fun remoteControlRows(s: SettingsRepository): List<SettingsRow> =
    panePrelude() +
        listOf(
            SettingsRow.Value(id = RowIds.REMOTE_TV_GUIDE, title = "TV guide", locked = true),
            SettingsRow.Value(id = RowIds.REMOTE_PLAYER, title = "Player", locked = true),
            SettingsRow.Header("Seeking options"),
            SettingsRow.Toggle(
                id = RowIds.REMOTE_SEEK_RWFF,
                title = "Use RW/FF/Pause for seeking/pause while watching catch-up",
                checked = s.get(TellySettings.SEEK_RWFF_CATCHUP),
            ),
            SettingsRow.Toggle(
                id = RowIds.REMOTE_RW_LIVE,
                title = "Use RW to rewind live stream with catch-up",
                checked = s.get(TellySettings.RW_REWINDS_LIVE),
            ),
            SettingsRow.Toggle(
                id = RowIds.REMOTE_SEEK_LEFT_RIGHT,
                title = "Use Left/Right for seeking while watching catch-up",
                checked = s.get(TellySettings.SEEK_LEFT_RIGHT),
            ),
            SettingsRow.Toggle(
                id = RowIds.REMOTE_LEFT_LIVE,
                title = "Use Left to rewind live stream with catch-up",
                checked = s.get(TellySettings.LEFT_REWINDS_LIVE),
            ),
            SettingsRow.Toggle(
                id = RowIds.REMOTE_SEEK_DOWN_UP,
                title = "Use Down/Up for seeking while watching catch-up",
                checked = s.get(TellySettings.SEEK_DOWN_UP),
            ),
            SettingsRow.Toggle(
                id = RowIds.REMOTE_DOWN_LIVE,
                title = "Use Down to rewind live stream with catch-up",
                checked = s.get(TellySettings.DOWN_REWINDS_LIVE),
            ),
        )
