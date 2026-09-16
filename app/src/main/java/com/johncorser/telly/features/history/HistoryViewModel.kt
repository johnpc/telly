package com.johncorser.telly.features.history

import com.johncorser.telly.core.kv.KeyValueStore
import com.johncorser.telly.features.playback.PlaybackSources
import com.johncorser.telly.features.playback.TuneController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.TimeZone

/**
 * The History screen's state (history-round2 §3): the full capped watch
 * history newest-first, each row resolving the programme that aired at the
 * watch time, plus clear-all (immediate — the reference's confirm behavior
 * is uncaptured) and row tuning. A plain class, unit-tested on the JVM.
 */
class HistoryViewModel(
    private val sources: PlaybackSources,
    private val store: KeyValueStore,
    zone: TimeZone,
    private val scope: CoroutineScope,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val rows: StateFlow<List<HistoryRow>> =
        sources.channelDao
            .observeVisible()
            .flatMapLatest { list -> sources.history.events.map { events -> list to events } }
            .flatMapLatest { (list, events) ->
                val span = HistoryRows.programmeSpan(events)
                sources.epgRepository
                    .programsFor(list.mapNotNull { it.epgId }, span.first, span.last)
                    .map { programs -> HistoryRows.rows(events, list, programs, zone) }
            }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** The clear-all trash icon empties the one watch_history table. */
    fun clearAll() {
        scope.launch { sources.history.clear() }
    }

    /**
     * OK on a row: persist the channel like search does; the playback screen
     * under this route re-tunes from lastChannelId when it recomposes.
     */
    fun tune(row: HistoryRow) {
        store.putLong(TuneController.LAST_CHANNEL_KEY, row.channel.id)
    }
}
