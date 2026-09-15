package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.SettingsRepository

/**
 * Manage groups sub-pane: one toggle per group title of the playlist,
 * default on. Disabled groups' channels disappear from the guide, panel
 * and search (filtered at the visible-channel query seam).
 */
fun playlistGroupRows(
    s: SettingsRepository,
    item: PlaylistItem,
): List<SettingsRow> =
    item.groups.map { group ->
        SettingsRow.Toggle(
            id = RowIds.PLAYLIST_GROUP_PREFIX + group,
            title = group,
            checked = s.get(playlistGroupEnabledSetting(item.url, group)),
        )
    }
