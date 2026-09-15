package com.johncorser.telly.core.settings

/**
 * Every persisted setting, with the defaults captured from TiviMate 5.2.0
 * (docs/reference/tivimate-capture-catalogue.md §6). Keys the UI cannot
 * honor yet still persist so the captured tree renders complete.
 */
object TellySettings {
    // General (catalogue 54–56).
    val AUTOSTART_ON_BOOT = boolSetting("general_autostart_boot", false)
    val AUTOSTART_ON_WAKE = boolSetting("general_autostart_wake", false)
    val LAST_CHANNEL_ON_START = boolSetting("general_last_channel_on_start", false)
    val PIP_ON_HOME = boolSetting("general_pip_on_home", false)
    val CONFIRM_EXIT = boolSetting("general_confirm_exit", false)
    val USER_AGENT = stringSetting("general_user_agent", "")
    val UDP_PROXY = stringSetting("general_udp_proxy", "")

    // Playlists (catalogue 19–22).
    val PLAYLISTS_SORTING = stringSetting("playlists_sorting", "By name")

    // EPG (catalogue 57–58). Interval 0 = "None": fetch only when never fetched.
    val EPG_UPDATE_INTERVAL_HOURS = intSetting("epg_update_interval_hours", 0)
    val EPG_PAST_DAYS_TO_KEEP = intSetting("epg_past_days_to_keep", 7)
    val EPG_STORE_DESCRIPTIONS = boolSetting("epg_store_descriptions", true)
    val EPG_UPDATE_ON_APP_START = boolSetting("epg_update_on_app_start", false)
    val EPG_UPDATE_ON_PLAYLISTS_CHANGE = boolSetting("epg_update_on_playlists_change", false)

    // Appearance (catalogue 59). Clock format/channel numbers are store-only
    // keys (no captured row) consumed by the guide slice.
    val LANGUAGE = stringSetting("appearance_language", "System")
    val FONT_SIZE = stringSetting("appearance_font_size", "Medium")
    val ACCENT_COLOR = stringSetting("appearance_accent_color", "Blue")
    val CLOCK_FORMAT = stringSetting("appearance_clock_format", "12-hour")
    val SHOW_CHANNEL_NUMBERS = boolSetting("appearance_channel_numbers", true)

    // Appearance sub-panes (ux-spec §3 Appearance: TV Guide / Player /
    // Groups / Logos). Every default reproduces telly's current rendering.
    val GUIDE_VISIBLE_CHANNELS = intSetting("appearance_guide_visible_channels", 7)
    val GUIDE_TRANSPARENCY = stringSetting("appearance_guide_transparency", "Opaque")
    val PLAYER_TRANSPARENCY = intSetting("appearance_player_transparency", 0)
    val PLAYER_PANEL_TIMEOUT_SEC = intSetting("appearance_player_timeout_sec", 5)
    val PLAYER_SHOW_CLOCK = boolSetting("appearance_player_show_clock", true)
    val SHOW_ALL_CHANNELS_GROUP = boolSetting("appearance_group_all_channels", true)
    val SHOW_FAVORITES_GROUP = boolSetting("appearance_group_favorites", true)
    val LOGO_BACKGROUND = stringSetting("appearance_logo_background", "Default")
    val LOGO_ROUNDED_CORNERS = boolSetting("appearance_logo_rounded", true)

    // Playback (catalogue 60–62). Resize mode is a store-only key.
    val BUFFER_SIZE = stringSetting("playback_buffer_size", "Small")
    val AUDIO_DECODER = stringSetting("playback_audio_decoder", "Hardware")
    val VIDEO_DECODER = stringSetting("playback_video_decoder", "Hardware")
    val AUTO_FRAME_RATE = stringSetting("playback_afr", "Off")
    val SURROUND_BY_DEFAULT = boolSetting("playback_surround_default", false)
    val AUDIO_PASSTHROUGH = boolSetting("playback_audio_passthrough", false)
    val USE_EXTERNAL_PLAYER = stringSetting("playback_external_player", "Off")
    val RESIZE_MODE = stringSetting("playback_resize_mode", "Fit")
    val SKIP_STEPS = stringSetting("playback_skip_steps", "10s / 30s / 1m / 5m")

    // Remote control seeking options (catalogue 64–65).
    val SEEK_RWFF_CATCHUP = boolSetting("remote_seek_rwff_catchup", true)
    val RW_REWINDS_LIVE = boolSetting("remote_rw_rewinds_live", false)
    val SEEK_LEFT_RIGHT = boolSetting("remote_seek_left_right", false)
    val LEFT_REWINDS_LIVE = boolSetting("remote_left_rewinds_live", false)
    val SEEK_DOWN_UP = boolSetting("remote_seek_down_up", false)
    val DOWN_REWINDS_LIVE = boolSetting("remote_down_rewinds_live", false)

    // Remote control key remaps (TV guide + Player sub-screens). Raw values
    // are the picker labels; every default equals the device-verified key
    // map (the keymap enums assert the same labels).
    val REMOTE_PLAYER_OK = stringSetting("remote_player_ok", "Show info panel")
    val REMOTE_PLAYER_UP_DOWN = stringSetting("remote_player_up_down", "Show info panel")
    val REMOTE_PLAYER_LEFT_RIGHT = stringSetting("remote_player_left_right", "Nothing")
    val REMOTE_PLAYER_LONG_OK = stringSetting("remote_player_long_ok", "Open quick menu")
    val REMOTE_GUIDE_LEFT_RIGHT = stringSetting("remote_guide_left_right", "Move by programme")
    val REMOTE_GUIDE_CHANNEL_UP_DOWN = stringSetting("remote_guide_channel_up_down", "Nothing")
    val REMOTE_GUIDE_LONG_OK = stringSetting("remote_guide_long_ok", "Open channel menu")

    // Parental controls (catalogue 67/69). The PIN is stored salted+hashed.
    val PARENTAL_ENABLED = boolSetting("parental_enabled", false)
    val PARENTAL_PIN_HASH = stringSetting("parental_pin_hash", "")
    val PARENTAL_PIN_SALT = stringSetting("parental_pin_salt", "")
    val PARENTAL_PIN_INPUT_METHOD = stringSetting("parental_pin_input_method", "Picker")
    val PARENTAL_RELOCK = stringSetting("parental_relock", "Always require")
    val PARENTAL_CHANNELS_ONLY = boolSetting("parental_channels_only", false)
    val PARENTAL_REQUIRE_FOR_SETTINGS = boolSetting("parental_require_settings", false)
    val PARENTAL_REQUIRE_FOR_PLAYLISTS = boolSetting("parental_require_settings_playlists", false)
    val PARENTAL_LOCKED_GROUPS = stringSetSetting("parental_locked_groups")

    // Reminders (premium in the reference; the popup lead follows the
    // ux-spec's "shortly before the programme starts", default 5 min).
    val REMINDER_LEAD_MINUTES = intSetting("reminders_lead_minutes", 5)

    // Other -> VOD (telly's unlocked pane; the reference sells VOD as premium).
    val VOD_REMEMBER_POSITION = boolSetting("vod_remember_position", true)

    // About (catalogue 53) — the only free toggle in the reference.
    val SEND_STATISTICS = boolSetting("about_send_statistics", true)
}
