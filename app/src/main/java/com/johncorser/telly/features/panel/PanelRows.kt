package com.johncorser.telly.features.panel

import com.johncorser.telly.features.epg.NowNext
import com.johncorser.telly.features.playback.PlaybackInfoBuilder
import com.johncorser.telly.features.playback.ProgramTimes
import com.johncorser.telly.features.playlist.db.ChannelEntity
import java.util.TimeZone

/** Pure row assembly for the channel panel: filtering, renumbering, formatting. */
object PanelRows {
    fun channelsIn(
        list: List<ChannelEntity>,
        group: String,
    ): List<ChannelEntity> =
        when (group) {
            PanelViewModel.ALL_CHANNELS -> list
            PanelViewModel.FAVORITES -> list.filter { it.flags.favorite }
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
                nowTitle = now?.details?.let(PlaybackInfoBuilder::displayTitle),
                nowRange = now?.let { ProgramTimes.range(it.startMs, it.endMs, zone) },
                remaining = now?.let { "${ProgramTimes.remainingMinutes(it.endMs, atMs)} min" },
                description = now?.details?.description,
                nextTitle = nowNext.next?.details?.let(PlaybackInfoBuilder::displayTitle),
                progressPermille = now?.let { ProgramTimes.progressPermille(it.startMs, it.endMs, atMs) } ?: 0,
            )
        }
}
