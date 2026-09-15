package com.johncorser.telly.features.settings

import com.johncorser.telly.features.epg.EpgSource

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
        is SettingsPane.EpgSourceDetail ->
            epgSources.firstOrNull { it.id == pane.sourceId }?.name ?: "EPG source"
        else -> fixedPaneTitle(pane) ?: appearancePaneTitle(pane)
    }

/** Panes whose header text is a constant (no feed lookup). */
private fun fixedPaneTitle(pane: SettingsPane): String? =
    when (pane) {
        is SettingsPane.PlaylistGroups -> "Manage groups"
        SettingsPane.EpgSources -> "EPG sources"
        SettingsPane.RemoteTvGuide -> "TV guide"
        SettingsPane.RemotePlayer -> "Player"
        SettingsPane.Reminders -> "Reminders"
        SettingsPane.Vod -> "VOD"
        SettingsPane.Recording -> "Recording"
        SettingsPane.OtherSearch -> "Search"
        SettingsPane.BlockedChannels -> "Blocked channels"
        else -> null
    }
