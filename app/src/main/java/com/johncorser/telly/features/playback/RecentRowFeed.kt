package com.johncorser.telly.features.playback

import com.johncorser.telly.features.epg.EpgRepository
import com.johncorser.telly.features.epg.NowNext
import com.johncorser.telly.features.epg.ProgramTitle
import com.johncorser.telly.features.history.HistoryRows
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.TimeZone

/**
 * Streams the recent-channel cards (history-round2 §1): recently watched
 * channels newest-first excluding the one already playing, each paired
 * with its CURRENT programme for the accent-blue card title and the
 * focused card's air-time line.
 */
class RecentRowFeed(
    sources: RecentRowSources,
    private val epgRepository: EpgRepository,
    private val zone: TimeZone,
    scope: CoroutineScope,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val cards: StateFlow<List<RecentCard>> =
        combine(sources.channels, sources.events, sources.current, sources.instant) { channels, events, current, at ->
            HistoryRows.recentChannels(events, channels, current?.id) to at
        }.flatMapLatest { (recent, at) ->
            epgRepository
                .nowNext(recent.mapNotNull { it.epgId }, at)
                .map { guide -> build(recent, guide) }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    private fun build(
        recent: List<ChannelEntity>,
        guide: Map<String, NowNext>,
    ): List<RecentCard> =
        recent.map { channel ->
            val now = guide[channel.epgId]?.now
            RecentCard(
                channel = channel,
                nowTitle = now?.details?.let(ProgramTitle::of),
                nowRange = now?.let { ProgramTimes.range(it.startMs, it.endMs, zone) },
            )
        }
}
