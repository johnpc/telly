package com.johncorser.telly.features.settings

import kotlinx.coroutines.flow.update

/**
 * Dispatch for the playlist-extras rows: URL edit, per-playlist
 * User-Agent, update options and Manage groups (the reference sells these
 * as premium; telly ships them unlocked per the charter precedent).
 */

internal fun SettingsViewModel.runPlaylistExtrasAction(rowId: String) {
    if (rowId.startsWith(RowIds.PLAYLIST_GROUP_PREFIX)) {
        flipPlaylistGroup(rowId.removePrefix(RowIds.PLAYLIST_GROUP_PREFIX))
        return
    }
    val url = currentDetailUrl() ?: return
    when (rowId) {
        RowIds.PLAYLIST_URL ->
            showOverlay(SettingsOverlay.TextEdit(rowId = rowId, title = "Playlist URL", value = url))
        RowIds.PLAYLIST_USER_AGENT ->
            showOverlay(
                SettingsOverlay.TextEdit(
                    rowId = rowId,
                    title = "User-Agent",
                    value = settings.get(playlistUserAgentSetting(url)),
                ),
            )
        RowIds.PLAYLIST_MANAGE_GROUPS -> push(SettingsPane.PlaylistGroups(url))
        RowIds.PLAYLIST_UPDATE_INTERVAL ->
            showOverlay(
                SettingsOverlay.Picker(
                    spec = playlistUpdateIntervalPicker(url),
                    current = settings.get(playlistUpdateIntervalSetting(url)).toString(),
                ),
            )
        RowIds.PLAYLIST_UPDATE_ON_START -> flip(playlistUpdateOnStartSetting(url))
    }
}

/** Manage groups: OK on a group row flips its enabled flag. */
private fun SettingsViewModel.flipPlaylistGroup(group: String) {
    val pane = mutableState.value.activePane as? SettingsPane.PlaylistGroups ?: return
    flip(playlistGroupEnabledSetting(pane.url, group))
}

/** Commits the User-Agent TextEdit ("" clears back to "Not set"). */
internal fun SettingsViewModel.submitPlaylistUserAgent(value: String) {
    currentDetailUrl()?.let { url -> settings.set(playlistUserAgentSetting(url), value.trim()) }
}

/**
 * Commits the Playlist URL TextEdit: fetch the new URL first, then re-key
 * everything tied to the old one (see PlaylistUrlChanger). A failed fetch
 * keeps the old URL, the update-playlist idiom. On success the open detail
 * pane is re-targeted so it keeps rendering the moved playlist.
 */
internal fun SettingsViewModel.submitPlaylistUrl(raw: String) {
    val oldUrl = currentDetailUrl() ?: return
    val newUrl = raw.trim()
    if (newUrl.isEmpty() || newUrl == oldUrl) return
    launch {
        if (changePlaylistUrl(oldUrl, newUrl)) retargetPlaylistPanes(oldUrl, newUrl)
    }
}

private fun SettingsViewModel.retargetPlaylistPanes(
    oldUrl: String,
    newUrl: String,
) {
    mutableState.update { state ->
        state.copy(
            panes =
                state.panes.map { pane ->
                    when {
                        pane is SettingsPane.PlaylistDetail && pane.url == oldUrl -> pane.copy(url = newUrl)
                        pane is SettingsPane.PlaylistGroups && pane.url == oldUrl -> pane.copy(url = newUrl)
                        else -> pane
                    }
                },
        )
    }
}
