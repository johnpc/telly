package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.SettingsRepository

/** Routes a pane to its row builder — the whole captured settings tree. */
fun rowsFor(
    pane: SettingsPane,
    settings: SettingsRepository,
    playlists: List<PlaylistItem>,
    versionName: String,
): List<SettingsRow> =
    when (pane) {
        is SettingsPane.Section -> sectionRows(pane.section, settings, playlists, versionName)
        is SettingsPane.PlaylistDetail ->
            playlists
                .firstOrNull { it.url == pane.url }
                ?.let { playlistDetailRows(settings, it) }
                .orEmpty()
        SettingsPane.EpgSources -> epgSourcesRows(playlists)
    }

private fun sectionRows(
    section: SettingsSection,
    settings: SettingsRepository,
    playlists: List<PlaylistItem>,
    versionName: String,
): List<SettingsRow> =
    when (section) {
        SettingsSection.GENERAL -> generalRows(settings)
        SettingsSection.PLAYLISTS -> playlistsRows(settings, playlists)
        SettingsSection.EPG -> epgRows(settings)
        SettingsSection.APPEARANCE -> appearanceRows(settings)
        SettingsSection.PLAYBACK -> playbackRows(settings)
        SettingsSection.REMOTE_CONTROL -> remoteControlRows(settings)
        SettingsSection.PARENTAL_CONTROLS -> parentalRows(settings)
        SettingsSection.OTHER -> otherRows()
        SettingsSection.ABOUT -> aboutRows(settings, versionName)
    }

/** The right pane's header title for [pane]. */
fun paneTitle(
    pane: SettingsPane,
    playlists: List<PlaylistItem>,
): String =
    when (pane) {
        is SettingsPane.Section -> pane.section.title
        is SettingsPane.PlaylistDetail -> playlists.firstOrNull { it.url == pane.url }?.name ?: pane.url
        SettingsPane.EpgSources -> "EPG sources"
    }
