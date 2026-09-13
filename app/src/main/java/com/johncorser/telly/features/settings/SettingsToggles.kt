package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.Setting
import com.johncorser.telly.core.settings.TellySettings

/** Row id -> boolean setting; the view model flips these generically. */
object SettingsToggles {
    val byRowId: Map<String, Setting<Boolean>> =
        mapOf(
            RowIds.AUTOSTART_BOOT to TellySettings.AUTOSTART_ON_BOOT,
            RowIds.AUTOSTART_WAKE to TellySettings.AUTOSTART_ON_WAKE,
            RowIds.LAST_CHANNEL to TellySettings.LAST_CHANNEL_ON_START,
            RowIds.PIP_ON_HOME to TellySettings.PIP_ON_HOME,
            RowIds.CONFIRM_EXIT to TellySettings.CONFIRM_EXIT,
            RowIds.EPG_STORE_DESCRIPTIONS to TellySettings.EPG_STORE_DESCRIPTIONS,
            RowIds.EPG_UPDATE_ON_APP_START to TellySettings.EPG_UPDATE_ON_APP_START,
            RowIds.EPG_UPDATE_ON_PLAYLISTS_CHANGE to TellySettings.EPG_UPDATE_ON_PLAYLISTS_CHANGE,
            RowIds.PLAYBACK_SURROUND to TellySettings.SURROUND_BY_DEFAULT,
            RowIds.PLAYBACK_PASSTHROUGH to TellySettings.AUDIO_PASSTHROUGH,
            RowIds.REMOTE_SEEK_RWFF to TellySettings.SEEK_RWFF_CATCHUP,
            RowIds.REMOTE_RW_LIVE to TellySettings.RW_REWINDS_LIVE,
            RowIds.REMOTE_SEEK_LEFT_RIGHT to TellySettings.SEEK_LEFT_RIGHT,
            RowIds.REMOTE_LEFT_LIVE to TellySettings.LEFT_REWINDS_LIVE,
            RowIds.REMOTE_SEEK_DOWN_UP to TellySettings.SEEK_DOWN_UP,
            RowIds.REMOTE_DOWN_LIVE to TellySettings.DOWN_REWINDS_LIVE,
            RowIds.PARENTAL_MASTER to TellySettings.PARENTAL_ENABLED,
            RowIds.PARENTAL_CHANNELS_ONLY to TellySettings.PARENTAL_CHANNELS_ONLY,
            RowIds.PARENTAL_REQUIRE_SETTINGS to TellySettings.PARENTAL_REQUIRE_FOR_SETTINGS,
            RowIds.PARENTAL_REQUIRE_PLAYLISTS to TellySettings.PARENTAL_REQUIRE_FOR_PLAYLISTS,
            RowIds.ABOUT_STATISTICS to TellySettings.SEND_STATISTICS,
        )
}
