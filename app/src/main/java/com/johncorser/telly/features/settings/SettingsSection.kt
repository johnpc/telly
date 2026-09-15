package com.johncorser.telly.features.settings

/**
 * The settings sections, in the exact captured order (catalogue §6, dump 18).
 * The reference lists an "Unlock Premium" row above these; telly is fully
 * open source with no premium tier, so that row is dropped.
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
