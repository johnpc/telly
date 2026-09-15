package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.features.epg.EpgSource

/**
 * Routes a sheet to its row builder — the whole captured settings tree.
 * A null pane is the root sheet: the nine captured sections (uidump 18).
 */
fun rowsFor(
    pane: SettingsPane?,
    settings: SettingsRepository,
    playlists: List<PlaylistItem>,
    versionName: String,
    epgSources: List<EpgSource> = emptyList(),
): List<SettingsRow> =
    when (pane) {
        null -> rootRows()
        is SettingsPane.Section -> sectionRows(pane.section, settings, playlists, versionName)
        is SettingsPane.PlaylistDetail ->
            playlists
                .firstOrNull { it.url == pane.url }
                ?.let { item -> playlistDetailRows(settings, item, epgSources.count { it.playlistUrl == item.url }) }
                .orEmpty()
        SettingsPane.EpgSources -> epgSourcesRows(playlists, epgSources)
        is SettingsPane.EpgSourceDetail ->
            epgSources
                .firstOrNull { it.id == pane.sourceId }
                ?.let { epgSourceDetailRows(it) }
                .orEmpty()
        SettingsPane.AppearanceTvGuide -> appearanceTvGuideRows(settings)
        SettingsPane.AppearancePlayer -> appearancePlayerRows(settings)
        SettingsPane.AppearanceGroups -> appearanceGroupsRows(settings)
        SettingsPane.AppearanceLogos -> appearanceLogosRows(settings)
    }

private fun rootRows(): List<SettingsRow> =
    SettingsSection.entries.map { section ->
        SettingsRow.Value(id = RowIds.SECTION_PREFIX + section.name, title = section.title)
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

/** The sheet's header title for [pane] (root sheet = "Settings"). */
fun paneTitle(
    pane: SettingsPane?,
    playlists: List<PlaylistItem>,
    epgSources: List<EpgSource> = emptyList(),
): String =
    when (pane) {
        null -> "Settings"
        is SettingsPane.Section -> pane.section.title
        is SettingsPane.PlaylistDetail -> playlists.firstOrNull { it.url == pane.url }?.name ?: pane.url
        SettingsPane.EpgSources -> "EPG sources"
        is SettingsPane.EpgSourceDetail ->
            epgSources.firstOrNull { it.id == pane.sourceId }?.name ?: "EPG source"
        SettingsPane.AppearanceTvGuide -> "TV guide"
        SettingsPane.AppearancePlayer -> "Player"
        SettingsPane.AppearanceGroups -> "Groups"
        SettingsPane.AppearanceLogos -> "Logos"
    }
