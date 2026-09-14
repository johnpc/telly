package com.johncorser.telly.features.guide

import com.johncorser.telly.features.epg.db.ProgramEntity
import com.johncorser.telly.features.history.HistoryGroup
import com.johncorser.telly.features.panel.PanelRows
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Feeds the grid from ProgramDao.observeWindow: the horizontal scroll
 * drives a quantized materialize span (visible window + prefetch margin),
 * and every (channels, group, span) change re-observes exactly that
 * window's programmes.
 */
class GuideRowsFeed(
    sources: GuideRowsSources,
    scrollX: StateFlow<Float>,
    private val programsFor: (List<String>, Long, Long) -> Flow<List<ProgramEntity>>,
    private val originMs: Long,
    scope: CoroutineScope,
) {
    private val span =
        scrollX
            .map { GuideWindowMath.materializeSpan(originMs, it, GuideGeometry.TIME_VIEWPORT_DP) }
            .distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    val rows: StateFlow<List<GuideRow>> =
        combine(sources.channels, sources.selectedGroup, sources.historyKeys, span) { list, group, history, window ->
            GuideRowsInput(list, group, history, window)
        }.flatMapLatest { input ->
            val tvgIds = input.channels.mapNotNull { it.source.tvgId }
            programsFor(tvgIds, input.span.fromMs, input.span.toMs)
                .map { programs -> GuideRowsBuilder.build(input, programs) }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** The groups column; History leads it only while it is selected. */
    val groups: StateFlow<List<String>> =
        combine(sources.channels, sources.selectedGroup) { list, group ->
            HistoryGroup.columnFor(group, PanelRows.groupNames(list))
        }.stateIn(scope, SharingStarted.Eagerly, PanelRows.groupNames(emptyList()))
}

/** The live inputs the grid rows are derived from. */
class GuideRowsSources(
    val channels: StateFlow<List<ChannelEntity>>,
    val selectedGroup: StateFlow<String>,
    val historyKeys: StateFlow<List<String>>,
)

/** One (channels, group, history, span) snapshot the rows build from. */
data class GuideRowsInput(
    val channels: List<ChannelEntity>,
    val group: String,
    val historyKeys: List<String>,
    val span: GuideSpan,
)
