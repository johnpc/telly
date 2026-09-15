package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.playlist.db.ChannelEntity

/**
 * Other -> Search sub-pane: the save-history toggle (default on; off stops
 * recording committed queries) and the confirmed clear action. The reference
 * pane is premium-locked and uncapturable — minimal matching rows.
 */
fun otherSearchRows(s: SettingsRepository): List<SettingsRow> =
    listOf(
        SettingsRow.Toggle(
            id = BlockRowIds.SEARCH_SAVE_HISTORY,
            title = "Save search history",
            checked = s.get(TellySettings.SEARCH_SAVE_HISTORY),
        ),
        SettingsRow.Action(id = BlockRowIds.SEARCH_CLEAR_HISTORY, title = "Clear search history"),
    )

/**
 * Parental controls -> Blocked channels: one row per blocked channel, OK
 * unblocks it (the pane entry itself is PIN-gated once).
 */
fun blockedChannelRows(blocked: List<ChannelEntity>): List<SettingsRow> =
    if (blocked.isEmpty()) {
        listOf(SettingsRow.Note("No blocked channels", accent = false))
    } else {
        blocked.map { channel ->
            SettingsRow.Value(
                id = BlockRowIds.CHANNEL_PREFIX + channel.id,
                title = channel.source.name,
                summary = "OK unblocks",
            )
        }
    }
