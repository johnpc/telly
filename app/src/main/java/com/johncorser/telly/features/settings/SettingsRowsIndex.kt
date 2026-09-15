package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.features.reminders.remindersRows
import com.johncorser.telly.features.vod.vodSettingsRows

/**
 * Routes a sheet to its row builder — the whole captured settings tree.
 * A null pane is the root sheet: the nine captured sections (uidump 18).
 * The Recording pane is built by the view model (it needs the DVR storage
 * hook), so here it resolves to an empty list.
 */
fun rowsFor(
    pane: SettingsPane?,
    settings: SettingsRepository,
    playlists: List<PlaylistItem>,
    versionName: String,
    feeds: SettingsFeeds = SettingsFeeds(),
): List<SettingsRow> =
    when (pane) {
        null -> rootRows()
        is SettingsPane.Section -> sectionRows(pane.section, settings, playlists, versionName)
        is SettingsPane.PlaylistDetail -> playlistDetailRowsFor(pane.url, settings, playlists, feeds)
        is SettingsPane.PlaylistGroups -> playlistGroupRowsFor(pane.url, settings, playlists)
        SettingsPane.EpgSources -> epgSourcesRows(playlists, feeds.epgSources)
        is SettingsPane.EpgSourceDetail -> epgSourceDetailRowsFor(pane.sourceId, feeds)
        else -> leafPaneRows(pane, settings, feeds)
    }

/** The leaf sub-panes: fixed row builders over the settings map + feeds. */
private fun leafPaneRows(
    pane: SettingsPane,
    settings: SettingsRepository,
    feeds: SettingsFeeds,
): List<SettingsRow> =
    when (pane) {
        SettingsPane.RemoteTvGuide -> remoteTvGuideRows(settings)
        SettingsPane.RemotePlayer -> remotePlayerRows(settings)
        SettingsPane.Reminders -> remindersRows(settings, feeds.reminders)
        SettingsPane.Vod -> vodSettingsRows(settings)
        SettingsPane.Recording -> emptyList()
        SettingsPane.OtherSearch -> otherSearchRows(settings)
        SettingsPane.BlockedChannels -> blockedChannelRows(feeds.blockedChannels)
        else -> appearancePaneRows(pane, settings)
    }

private fun playlistDetailRowsFor(
    url: String,
    settings: SettingsRepository,
    playlists: List<PlaylistItem>,
    feeds: SettingsFeeds,
): List<SettingsRow> =
    playlists
        .firstOrNull { it.url == url }
        ?.let { item ->
            playlistDetailRows(settings, item, feeds.epgSources.count { it.playlistUrl == item.url })
        }.orEmpty()

private fun playlistGroupRowsFor(
    url: String,
    settings: SettingsRepository,
    playlists: List<PlaylistItem>,
): List<SettingsRow> =
    playlists
        .firstOrNull { it.url == url }
        ?.let { item -> playlistGroupRows(settings, item) }
        .orEmpty()

private fun epgSourceDetailRowsFor(
    sourceId: Long,
    feeds: SettingsFeeds,
): List<SettingsRow> =
    feeds.epgSources
        .firstOrNull { it.id == sourceId }
        ?.let { epgSourceDetailRows(it) }
        .orEmpty()

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
