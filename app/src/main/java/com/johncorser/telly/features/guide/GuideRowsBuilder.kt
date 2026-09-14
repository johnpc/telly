package com.johncorser.telly.features.guide

import com.johncorser.telly.features.epg.db.ProgramEntity
import com.johncorser.telly.features.history.HistoryGroup
import com.johncorser.telly.features.panel.PanelRows
import com.johncorser.telly.features.panel.PanelViewModel
import com.johncorser.telly.features.playlist.db.ChannelEntity

/**
 * Assembles the grid rows for a group: the panel's group filter (shared
 * with the channel panel; the synthetic History group orders by watch
 * recency instead), TiviMate's per-group renumbering from 1 (capture 74),
 * and one cell strip per channel over the span.
 */
object GuideRowsBuilder {
    fun build(
        input: GuideRowsInput,
        programs: List<ProgramEntity>,
    ): List<GuideRow> {
        val byTvgId = programs.groupBy { it.channelTvgId }
        return channelsIn(input).mapIndexed { index, channel ->
            GuideRow(
                channel = channel,
                displayNumber = if (input.group == PanelViewModel.ALL_CHANNELS) channel.number else index + 1,
                cells = GuideCellsBuilder.build(byTvgId[channel.source.tvgId].orEmpty(), input.span),
            )
        }
    }

    private fun channelsIn(input: GuideRowsInput): List<ChannelEntity> =
        if (input.group == HistoryGroup.NAME) {
            HistoryGroup.channelsIn(input.historyKeys, input.channels)
        } else {
            PanelRows.channelsIn(input.channels, input.group)
        }
}
