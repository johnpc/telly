package com.johncorser.telly.features.panel

import com.johncorser.telly.features.epg.NowNext
import com.johncorser.telly.features.epg.ProgramTitle
import com.johncorser.telly.features.groups.CustomGroup
import com.johncorser.telly.features.mylist.ChannelReorder
import com.johncorser.telly.features.playback.ClockStyle
import com.johncorser.telly.features.playback.ProgramTimes
import com.johncorser.telly.features.playlist.ChannelImporter
import com.johncorser.telly.features.playlist.db.ChannelEntity

/** Pure row assembly for the channel panel: filtering, renumbering, formatting. */
object PanelRows {
    /**
     * Groups column order (capture 25): Favorites, All channels, playlist
     * groups — then the user's custom groups, in their own sort order.
     */
    fun groupNames(
        channels: List<ChannelEntity>,
        custom: List<CustomGroup> = emptyList(),
    ): List<String> =
        (
            listOf(PanelViewModel.FAVORITES, PanelViewModel.ALL_CHANNELS) +
                channels.mapNotNull { it.source.groupTitle } + custom.map { it.name }
        ).distinct()

    /**
     * A custom group's members match by refresh-stable channel key; a name
     * shared with a playlist group resolves to the playlist group.
     */
    fun channelsIn(
        list: List<ChannelEntity>,
        group: String,
        custom: List<CustomGroup> = emptyList(),
    ): List<ChannelEntity> =
        when (group) {
            PanelViewModel.ALL_CHANNELS -> list
            // Favorites honor the Manage-Favorites order (stable sort:
            // untouched favorites keep the base zap order).
            PanelViewModel.FAVORITES -> ChannelReorder.favorites(list)
            else ->
                list
                    .filter { it.source.groupTitle == group }
                    .ifEmpty { customMembers(list, group, custom) }
        }

    private fun customMembers(
        list: List<ChannelEntity>,
        group: String,
        custom: List<CustomGroup>,
    ): List<ChannelEntity> {
        val members = custom.firstOrNull { it.name == group }?.members ?: return emptyList()
        return list.filter { ChannelImporter.keyOf(it) in members }
    }

    fun build(
        groupChannels: List<ChannelEntity>,
        group: String,
        guide: Map<String, NowNext>,
        atMs: Long,
        style: ClockStyle,
    ): List<PanelRow> =
        groupChannels.mapIndexed { index, channel ->
            val nowNext = guide[channel.epgId] ?: NowNext()
            val now = nowNext.now
            PanelRow(
                channel = channel,
                displayNumber = if (group == PanelViewModel.ALL_CHANNELS) channel.number else index + 1,
                nowTitle = now?.details?.let(ProgramTitle::of),
                nowRange = now?.let { ProgramTimes.range(it.startMs, it.endMs, style) },
                nowStartMs = now?.startMs,
                nowEndMs = now?.endMs,
                remaining = now?.let { "${ProgramTimes.remainingMinutes(it.endMs, atMs)} min" },
                description = now?.details?.description,
                nextTitle = nowNext.next?.details?.let(ProgramTitle::of),
                progressPermille = now?.let { ProgramTimes.progressPermille(it.startMs, it.endMs, atMs) } ?: 0,
            )
        }
}
