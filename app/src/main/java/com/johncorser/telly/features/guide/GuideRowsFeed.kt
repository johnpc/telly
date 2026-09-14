package com.johncorser.telly.features.guide

import com.johncorser.telly.features.epg.db.ProgramEntity
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
    channels: StateFlow<List<ChannelEntity>>,
    selectedGroup: StateFlow<String>,
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
        combine(channels, selectedGroup, span) { list, group, window -> Triple(list, group, window) }
            .flatMapLatest { (list, group, window) ->
                val tvgIds = list.mapNotNull { it.source.tvgId }
                programsFor(tvgIds, window.fromMs, window.toMs)
                    .map { programs -> GuideRowsBuilder.build(list, group, programs, window) }
            }.stateIn(scope, SharingStarted.Eagerly, emptyList())
}
