package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.Setting
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.core.settings.boolSetting

/** Per-playlist enable flag, keyed by the playlist URL. */
fun playlistEnabledSetting(url: String): Setting<Boolean> = boolSetting("playlist_enabled:$url", true)

/** "No sources" / "1 source" / "N sources", as capture 20 words it. */
fun epgSourceCountSummary(count: Int): String =
    when (count) {
        0 -> "No sources"
        1 -> "1 source"
        else -> "$count sources"
    }

/** What the Playlists pane needs to know about one stored playlist. */
data class PlaylistItem(
    val url: String,
    val name: String,
    val channelCount: Int,
    val epgUrl: String? = null,
)

/** Playlists pane rows (catalogue 19): playlist list + list-level actions. */
fun playlistsRows(
    s: SettingsRepository,
    playlists: List<PlaylistItem>,
): List<SettingsRow> {
    val sorted =
        when (s.get(TellySettings.PLAYLISTS_SORTING)) {
            "By name" -> playlists.sortedBy { it.name.lowercase() }
            else -> playlists
        }
    val items =
        sorted.map { item ->
            SettingsRow.Value(
                id = RowIds.PLAYLIST_PREFIX + item.url,
                title = item.name,
                summary = "Channels: ${item.channelCount}",
                checkIcon = true,
            )
        }
    return items +
        listOf(
            SettingsRow.Value(
                id = RowIds.PLAYLISTS_SORTING,
                title = "Playlists sorting",
                summary = s.get(TellySettings.PLAYLISTS_SORTING),
            ),
            SettingsRow.Action(id = RowIds.ADD_PLAYLIST, title = "Add playlist"),
            SettingsRow.Action(id = RowIds.UPDATE_ALL_PLAYLISTS, title = "Update all playlists"),
        )
}

/** Per-playlist pane rows (catalogue 20–21). */
fun playlistDetailRows(
    s: SettingsRepository,
    item: PlaylistItem,
    customEpgSourceCount: Int = 0,
): List<SettingsRow> =
    listOf(
        SettingsRow.Toggle(
            id = RowIds.PLAYLIST_ENABLE,
            title = "Enable playlist",
            checked = s.get(playlistEnabledSetting(item.url)),
        ),
        SettingsRow.Value(id = RowIds.PLAYLIST_NAME, title = "Playlist name", summary = item.name),
        SettingsRow.Value(id = RowIds.PLAYLIST_URL, title = "Playlist URL", summary = item.url, locked = true),
        SettingsRow.Value(
            id = RowIds.PLAYLIST_EPG_SOURCES,
            title = "EPG sources",
            summary = epgSourceCountSummary((if (item.epgUrl == null) 0 else 1) + customEpgSourceCount),
        ),
        SettingsRow.Value(
            id = RowIds.PLAYLIST_USER_AGENT,
            title = "User-Agent",
            summary = "Not set",
            locked = true,
        ),
        SettingsRow.Value(id = RowIds.PLAYLIST_MANAGE_GROUPS, title = "Manage groups", locked = true),
        SettingsRow.Header("Update options"),
        SettingsRow.Value(
            id = RowIds.PLAYLIST_UPDATE_INTERVAL,
            title = "Update interval, hours",
            summary = "None",
            locked = true,
        ),
        SettingsRow.Toggle(
            id = RowIds.PLAYLIST_UPDATE_ON_START,
            title = "Update on app start",
            checked = false,
            locked = true,
        ),
        SettingsRow.Action(id = RowIds.PLAYLIST_UPDATE_NOW, title = "Update playlist"),
        SettingsRow.Action(id = RowIds.PLAYLIST_DELETE, title = "Delete playlist"),
    )
