package com.johncorser.telly.features.settings

/** Stable row identifiers the view model dispatches on. */
object RowIds {
    /** Root-sheet section rows append the [SettingsSection] name. */
    const val SECTION_PREFIX = "section:"

    // General.
    const val AUTOSTART_BOOT = "general.autostart_boot"
    const val AUTOSTART_WAKE = "general.autostart_wake"
    const val LAST_CHANNEL = "general.last_channel"
    const val PIP_ON_HOME = "general.pip_on_home"
    const val CONFIRM_EXIT = "general.confirm_exit"
    const val USER_AGENT = "general.user_agent"
    const val UDP_PROXY = "general.udp_proxy"
    const val BACK_UP_DATA = "general.back_up_data"
    const val RESTORE_DATA = "general.restore_data"

    // Playlists. Per-playlist rows append the playlist URL after the prefix.
    const val PLAYLISTS_SORTING = "playlists.sorting"
    const val ADD_PLAYLIST = "playlists.add"
    const val UPDATE_ALL_PLAYLISTS = "playlists.update_all"
    const val PLAYLIST_PREFIX = "playlist:"
    const val PLAYLIST_ENABLE = "playlist.enable"
    const val PLAYLIST_NAME = "playlist.name"
    const val PLAYLIST_URL = "playlist.url"
    const val PLAYLIST_EPG_SOURCES = "playlist.epg_sources"
    const val PLAYLIST_USER_AGENT = "playlist.user_agent"
    const val PLAYLIST_MANAGE_GROUPS = "playlist.manage_groups"
    const val PLAYLIST_UPDATE_INTERVAL = "playlist.update_interval"
    const val PLAYLIST_UPDATE_ON_START = "playlist.update_on_start"
    const val PLAYLIST_UPDATE_NOW = "playlist.update_now"
    const val PLAYLIST_DELETE = "playlist.delete"

    // EPG.
    const val EPG_SOURCES = "epg.sources"
    const val EPG_PAST_DAYS = "epg.past_days"
    const val EPG_STORE_DESCRIPTIONS = "epg.store_descriptions"
    const val EPG_UPDATE_INTERVAL = "epg.update_interval"
    const val EPG_UPDATE_ON_APP_START = "epg.update_on_app_start"
    const val EPG_UPDATE_ON_PLAYLISTS_CHANGE = "epg.update_on_playlists_change"
    const val EPG_UPDATE_NOW = "epg.update_now"
    const val EPG_ADD_SOURCE = "epg.add_source"
    const val EPG_SOURCE_PREFIX = "epg_source:"

    /** Custom source rows append the source's row id after the prefix. */
    const val EPG_CUSTOM_SOURCE_PREFIX = "epg_custom_source:"
    const val EPG_SOURCE_URL = "epg_source.url"
    const val EPG_SOURCE_DELETE = "epg_source.delete"

    // Appearance.
    const val APPEARANCE_TV_GUIDE = "appearance.tv_guide"
    const val APPEARANCE_PLAYER = "appearance.player"
    const val APPEARANCE_GROUPS = "appearance.groups"
    const val APPEARANCE_LOGOS = "appearance.logos"
    const val APPEARANCE_LANGUAGE = "appearance.language"
    const val APPEARANCE_FONT_SIZE = "appearance.font_size"
    const val APPEARANCE_COLOR_THEME = "appearance.color_theme"

    // Playback.
    const val PLAYBACK_BUFFER_SIZE = "playback.buffer_size"
    const val PLAYBACK_AUDIO_DECODER = "playback.audio_decoder"
    const val PLAYBACK_VIDEO_DECODER = "playback.video_decoder"
    const val PLAYBACK_AFR = "playback.afr"
    const val PLAYBACK_SURROUND = "playback.surround"
    const val PLAYBACK_PASSTHROUGH = "playback.passthrough"
    const val PLAYBACK_EXTERNAL_PLAYER = "playback.external_player"
    const val PLAYBACK_SKIP_STEPS = "playback.skip_steps"

    // Remote control.
    const val REMOTE_TV_GUIDE = "remote.tv_guide"
    const val REMOTE_PLAYER = "remote.player"
    const val REMOTE_SEEK_RWFF = "remote.seek_rwff"
    const val REMOTE_RW_LIVE = "remote.rw_live"
    const val REMOTE_SEEK_LEFT_RIGHT = "remote.seek_left_right"
    const val REMOTE_LEFT_LIVE = "remote.left_live"
    const val REMOTE_SEEK_DOWN_UP = "remote.seek_down_up"
    const val REMOTE_DOWN_LIVE = "remote.down_live"

    // Parental controls.
    const val PARENTAL_MASTER = "parental.master"
    const val PARENTAL_CHANGE_PIN = "parental.change_pin"
    const val PARENTAL_PIN_INPUT = "parental.pin_input"
    const val PARENTAL_RELOCK = "parental.relock"
    const val PARENTAL_CHANNELS_ONLY = "parental.channels_only"
    const val PARENTAL_REQUIRE_SETTINGS = "parental.require_settings"
    const val PARENTAL_REQUIRE_PLAYLISTS = "parental.require_playlists"

    // Other.
    const val OTHER_SEARCH = "other.search"
    const val OTHER_REMINDERS = "other.reminders"
    const val OTHER_RECORDING = "other.recording"
    const val OTHER_VOD = "other.vod"

    // Other -> VOD (unlocked in telly; the reference sells VOD as premium).
    const val VOD_REMEMBER_POSITION = "vod.remember_position"
    const val VOD_CLEAR_POSITIONS = "vod.clear_positions"

    // About.
    const val ABOUT_STATISTICS = "about.statistics"
    const val ABOUT_PRIVACY_POLICY = "about.privacy_policy"
    const val ABOUT_VERSION = "about.version"
}
