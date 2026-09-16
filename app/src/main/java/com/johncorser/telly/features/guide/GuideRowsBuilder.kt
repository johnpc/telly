package com.johncorser.telly.features.guide

import com.johncorser.telly.features.epg.db.ProgramEntity
import com.johncorser.telly.features.panel.PanelRows
import com.johncorser.telly.features.panel.PanelViewModel

/**
 * Assembles the grid rows for a group: the panel's group filter (shared
 * with the channel panel), TiviMate's per-group renumbering from 1
 * (capture 74), and one cell strip per channel over the span.
 */
object GuideRowsBuilder {
    fun build(
        input: GuideRowsInput,
        programs: List<ProgramEntity>,
    ): List<GuideRow> {
        val byTvgId = programs.groupBy { it.channelTvgId }
        return PanelRows.channelsIn(input.channels, input.group, input.custom).mapIndexed { index, channel ->
            GuideRow(
                channel = channel,
                displayNumber = if (input.group == PanelViewModel.ALL_CHANNELS) channel.number else index + 1,
                cells = GuideCellsBuilder.build(byTvgId[channel.epgId].orEmpty(), input.span),
            )
        }
    }
}
