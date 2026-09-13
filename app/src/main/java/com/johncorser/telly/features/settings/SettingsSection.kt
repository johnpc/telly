package com.johncorser.telly.features.settings

/**
 * The settings sections, in the exact captured order (catalogue §6, dump 18).
 * "Unlock Premium" is a row above these in the section list, not a section.
 */
enum class SettingsSection(
    val title: String,
) {
    GENERAL("General"),
    PLAYLISTS("Playlists"),
    EPG("EPG"),
    APPEARANCE("Appearance"),
    PLAYBACK("Playback"),
    REMOTE_CONTROL("Remote control"),
    PARENTAL_CONTROLS("Parental controls"),
    OTHER("Other"),
    ABOUT("About"),
}
