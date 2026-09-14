package com.johncorser.telly.features.history

import com.johncorser.telly.features.playlist.ChannelImporter
import com.johncorser.telly.features.playlist.db.ChannelEntity

/**
 * The guide's synthetic "History" source group (capture 47 note: the
 * History card "opens the same overlay with History as source group when
 * it exists"). It is never a persistent group: capture 25 lists no History
 * in the groups column, so it leads the column only while it is the
 * selected source group.
 */
object HistoryGroup {
    const val NAME = "History"

    /** Playlist-refresh-stable channel identity, same key as user flags. */
    fun identityOf(channel: ChannelEntity): String =
        ChannelImporter.identityOf(channel.source.tvgId, channel.source.streamUrl, channel.source.name)

    /** Orders the visible channels newest-watch-first; unwatched ones drop. */
    fun channelsIn(
        keys: List<String>,
        channels: List<ChannelEntity>,
    ): List<ChannelEntity> {
        val byKey = channels.associateBy(::identityOf)
        return keys.mapNotNull(byKey::get)
    }

    /** The groups column, with History prepended only while it is selected. */
    fun columnFor(
        selected: String,
        names: List<String>,
    ): List<String> = if (selected == NAME) listOf(NAME) + names else names
}
