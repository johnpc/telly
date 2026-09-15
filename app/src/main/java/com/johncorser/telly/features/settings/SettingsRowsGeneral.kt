package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings

/** "Not set" is the captured placeholder for blank value rows. */
fun notSet(value: String): String = value.ifBlank { "Not set" }

/** General pane rows (catalogue 54–56), all wired to the store. */
fun generalRows(s: SettingsRepository): List<SettingsRow> =
    listOf(
        SettingsRow.Toggle(
            id = RowIds.AUTOSTART_BOOT,
            title = "Auto start app on boot",
            checked = s.get(TellySettings.AUTOSTART_ON_BOOT),
        ),
        SettingsRow.Toggle(
            id = RowIds.AUTOSTART_WAKE,
            title = "Auto start app on wake up from sleep mode",
            summary = "May not work on all devices",
            checked = s.get(TellySettings.AUTOSTART_ON_WAKE),
        ),
        SettingsRow.Toggle(
            id = RowIds.LAST_CHANNEL,
            title = "Turn on last channel on app start",
            checked = s.get(TellySettings.LAST_CHANNEL_ON_START),
        ),
        SettingsRow.Toggle(
            id = RowIds.PIP_ON_HOME,
            title = "Switch to picture-in-picture mode on press Home",
            checked = s.get(TellySettings.PIP_ON_HOME),
        ),
        SettingsRow.Toggle(
            id = RowIds.CONFIRM_EXIT,
            title = "Confirm exit by second press Back",
            checked = s.get(TellySettings.CONFIRM_EXIT),
        ),
        SettingsRow.Value(
            id = RowIds.USER_AGENT,
            title = "User-Agent",
            summary = notSet(s.get(TellySettings.USER_AGENT)),
        ),
        SettingsRow.Value(
            id = RowIds.UDP_PROXY,
            title = "UDP proxy (address:port)",
            summary = notSet(s.get(TellySettings.UDP_PROXY)),
        ),
        SettingsRow.Action(id = RowIds.BACK_UP_DATA, title = "Back up data"),
        SettingsRow.Action(id = RowIds.RESTORE_DATA, title = "Restore data"),
    )
