package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.epg.EpgSource

/** EPG pane rows (catalogue 57). */
fun epgRows(s: SettingsRepository): List<SettingsRow> =
    listOf(
        SettingsRow.Value(id = RowIds.EPG_SOURCES, title = "EPG sources"),
        SettingsRow.Value(
            id = RowIds.EPG_PAST_DAYS,
            title = "Past days to keep EPG",
            summary = s.get(TellySettings.EPG_PAST_DAYS_TO_KEEP).toString(),
        ),
        SettingsRow.Toggle(
            id = RowIds.EPG_STORE_DESCRIPTIONS,
            title = "Store program descriptions",
            checked = s.get(TellySettings.EPG_STORE_DESCRIPTIONS),
        ),
        SettingsRow.Header("Update options"),
        SettingsRow.Value(
            id = RowIds.EPG_UPDATE_INTERVAL,
            title = "Update interval, hours",
            summary = SettingsPickers.intervalLabel(s.get(TellySettings.EPG_UPDATE_INTERVAL_HOURS)),
        ),
        SettingsRow.Toggle(
            id = RowIds.EPG_UPDATE_ON_APP_START,
            title = "Update on app start",
            checked = s.get(TellySettings.EPG_UPDATE_ON_APP_START),
        ),
        SettingsRow.Toggle(
            id = RowIds.EPG_UPDATE_ON_PLAYLISTS_CHANGE,
            title = "Update on playlists change",
            checked = s.get(TellySettings.EPG_UPDATE_ON_PLAYLISTS_CHANGE),
        ),
        SettingsRow.Action(id = RowIds.EPG_UPDATE_NOW, title = "Update EPG"),
    )

/**
 * EPG sources pane (catalogue 58): one row per playlist url-tvg source,
 * then the playlist's custom sources in added order. The reference free
 * tier LOCKS "Add source"; telly ships it unlocked by product directive,
 * following TiviMate's documented premium flow (ux-spec 3.10).
 */
fun epgSourcesRows(
    playlists: List<PlaylistItem>,
    sources: List<EpgSource>,
): List<SettingsRow> {
    val auto =
        playlists.filter { it.epgUrl != null }.map { item ->
            SettingsRow.Value(
                id = RowIds.EPG_SOURCE_PREFIX + item.url,
                title = "${item.name} (default)",
                summary = item.epgUrl,
                checkIcon = true,
            )
        }
    val custom =
        sources.map { source ->
            SettingsRow.Value(
                id = RowIds.EPG_CUSTOM_SOURCE_PREFIX + source.id,
                title = source.name,
                summary = source.url,
                checkIcon = true,
            )
        }
    return auto + custom +
        listOf(
            SettingsRow.Action(id = RowIds.EPG_ADD_SOURCE, title = "Add source"),
            SettingsRow.Note("EPG sources should be assigned in the playlist settings", accent = false),
        )
}

/** One custom source's pane: edit its URL or delete it (uncapturable —
 * the reference locks per-source management behind premium; the shape
 * follows the playlist-detail pane precedent). */
fun epgSourceDetailRows(source: EpgSource): List<SettingsRow> =
    listOf(
        SettingsRow.Value(id = RowIds.EPG_SOURCE_URL, title = "Source URL", summary = source.url),
        SettingsRow.Action(id = RowIds.EPG_SOURCE_DELETE, title = "Delete source"),
    )
