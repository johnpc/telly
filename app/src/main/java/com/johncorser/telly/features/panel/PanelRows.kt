package com.johncorser.telly.features.panel

import com.johncorser.telly.features.epg.NowNext
import com.johncorser.telly.features.epg.ProgramTitle
import com.johncorser.telly.features.mylist.ChannelReorder
import com.johncorser.telly.features.playback.ProgramTimes
import com.johncorser.telly.features.playlist.db.ChannelEntity
import java.util.TimeZone

/** Pure row assembly for the channel panel: filtering, renumbering, formatting. */
object PanelRows {
    /** Groups column order (capture 25): Favorites, All channels, playlist groups. */
    fun groupNames(channels: List<ChannelEntity>): List<String> =
        listOf(PanelViewModel.FAVORITES, PanelViewModel.ALL_CHANNELS) +
            channels.mapNotNull { it.source.groupTitle }.distinct()

    fun channelsIn(
        list: List<ChannelEntity>,
        group: String,
    ): List<ChannelEntity> =
        when (group) {
            PanelViewModel.ALL_CHANNELS -> list
            // Favorites honor the Manage-Favorites order (stable sort:
            // untouched favorites keep the base zap order).
            PanelViewModel.FAVORITES -> ChannelReorder.favorites(list)
            else -> list.filter { it.source.groupTitle == group }
        }

    fun build(
        groupChannels: List<ChannelEntity>,
        group: String,
        guide: Map<String, NowNext>,
        atMs: Long,
        zone: TimeZone,
    ): List<PanelRow> =
        groupChannels.mapIndexed { index, channel ->
            val nowNext = guide[channel.source.tvgId] ?: NowNext()
            val now = nowNext.now
            PanelRow(
                channel = channel,
                displayNumber = if (group == PanelViewModel.ALL_CHANNELS) channel.number else index + 1,
                nowTitle = now?.details?.let(ProgramTitle::of),
                nowRange = now?.let { ProgramTimes.range(it.startMs, it.endMs, zone) },
                nowStartMs = now?.startMs,
                nowEndMs = now?.endMs,
                remaining = now?.let { "${ProgramTimes.remainingMinutes(it.endMs, atMs)} min" },
                description = now?.details?.description,
                nextTitle = nowNext.next?.details?.let(ProgramTitle::of),
                progressPermille = now?.let { ProgramTimes.progressPermille(it.startMs, it.endMs, atMs) } ?: 0,
            )
        }
}
