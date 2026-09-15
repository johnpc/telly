package com.johncorser.telly.features.mylist

import com.johncorser.telly.core.kv.KeyValueStore
import com.johncorser.telly.features.playback.TuneController
import com.johncorser.telly.features.playlist.db.ChannelDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.TimeZone

/**
 * The My List screen's state: saved programmes newest-first with ended
 * entries auto-hidden, row tuning (airing entries) and per-entry removal.
 * A plain class, unit-tested on the JVM.
 */
class MyListViewModel(
    private val store: MyListStore,
    channelDao: ChannelDao,
    private val lastChannel: KeyValueStore,
    private val clock: () -> Long,
    zone: TimeZone,
    private val scope: CoroutineScope,
) {
    val rows: StateFlow<List<MyListRow>> =
        combine(store.entries, channelDao.observeVisible()) { entries, channels ->
            MyListRows.rows(entries, channels, clock(), zone)
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    /**
     * OK on an airing row: persist the channel like search does; the caller
     * then rebuilds the guide-root BACK chain with playback on top.
     */
    fun tune(row: MyListRow) {
        lastChannel.putLong(TuneController.LAST_CHANNEL_KEY, row.channel.id)
    }

    /** The delete affordance (long-OK on a row) removes the entry. */
    fun remove(row: MyListRow) {
        scope.launch { store.remove(row.entry.channelKey, row.entry.startMs) }
    }
}
