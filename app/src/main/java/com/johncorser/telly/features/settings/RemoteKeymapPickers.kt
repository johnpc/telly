package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.Setting
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.guide.GuideChannelKeysAction
import com.johncorser.telly.features.guide.GuideLeftRightAction
import com.johncorser.telly.features.guide.GuideLongOkAction
import com.johncorser.telly.features.playback.KeymapChoice
import com.johncorser.telly.features.playback.PlayerLeftRightAction
import com.johncorser.telly.features.playback.PlayerLongOkAction
import com.johncorser.telly.features.playback.PlayerOkAction
import com.johncorser.telly.features.playback.PlayerUpDownAction

/**
 * Remote control key-remap pickers (the TV guide + Player sub-panes).
 * Options are the keymap enums' labels, persisted raw, so the pickers and
 * the key policies can never drift; defaults are the parsers' fallbacks =
 * the device-verified key map.
 */
internal object RemoteKeymapPickers {
    private val guideLeftRight =
        spec(
            rowId = RowIds.REMOTE_GUIDE_LEFT_RIGHT,
            title = "Left/Right buttons",
            setting = TellySettings.REMOTE_GUIDE_LEFT_RIGHT,
            choices = GuideLeftRightAction.entries,
        )

    private val guideChannelKeys =
        spec(
            rowId = RowIds.REMOTE_GUIDE_CHANNEL_UP_DOWN,
            title = "Channel up/down buttons",
            setting = TellySettings.REMOTE_GUIDE_CHANNEL_UP_DOWN,
            choices = GuideChannelKeysAction.entries,
        )

    private val guideLongOk =
        spec(
            rowId = RowIds.REMOTE_GUIDE_LONG_OK,
            title = "Long press OK",
            setting = TellySettings.REMOTE_GUIDE_LONG_OK,
            choices = GuideLongOkAction.entries,
        )

    private val playerOk =
        spec(
            rowId = RowIds.REMOTE_PLAYER_OK,
            title = "OK button",
            setting = TellySettings.REMOTE_PLAYER_OK,
            choices = PlayerOkAction.entries,
        )

    private val playerUpDown =
        spec(
            rowId = RowIds.REMOTE_PLAYER_UP_DOWN,
            title = "Up/Down buttons",
            setting = TellySettings.REMOTE_PLAYER_UP_DOWN,
            choices = PlayerUpDownAction.entries,
        )

    private val playerLeftRight =
        spec(
            rowId = RowIds.REMOTE_PLAYER_LEFT_RIGHT,
            title = "Left/Right buttons",
            setting = TellySettings.REMOTE_PLAYER_LEFT_RIGHT,
            choices = PlayerLeftRightAction.entries,
        )

    private val playerLongOk =
        spec(
            rowId = RowIds.REMOTE_PLAYER_LONG_OK,
            title = "Long press OK",
            setting = TellySettings.REMOTE_PLAYER_LONG_OK,
            choices = PlayerLongOkAction.entries,
        )

    val specs: List<PickerSpec> =
        listOf(guideLeftRight, guideChannelKeys, guideLongOk, playerOk, playerUpDown, playerLeftRight, playerLongOk)

    /** Every remap key's device-verified default raw (= a picker label). */
    val defaults: Map<String, String> =
        listOf(
            TellySettings.REMOTE_GUIDE_LEFT_RIGHT,
            TellySettings.REMOTE_GUIDE_CHANNEL_UP_DOWN,
            TellySettings.REMOTE_GUIDE_LONG_OK,
            TellySettings.REMOTE_PLAYER_OK,
            TellySettings.REMOTE_PLAYER_UP_DOWN,
            TellySettings.REMOTE_PLAYER_LEFT_RIGHT,
            TellySettings.REMOTE_PLAYER_LONG_OK,
        ).associate { it.key to it.default }

    private fun spec(
        rowId: String,
        title: String,
        setting: Setting<String>,
        choices: List<KeymapChoice>,
    ): PickerSpec = PickerSpec(rowId, title, setting.key, choices.map { PickerOption(it.label, it.label) })
}
