package com.johncorser.telly.features.panel

import com.johncorser.telly.features.epg.EpgRepository
import com.johncorser.telly.features.playback.ProgramTimes
import com.johncorser.telly.features.playlist.db.ChannelDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.TimeZone

/**
 * Channel-list panel state: the groups column (Favorites + All channels +
 * playlist groups, capture 25), the channel rows of the selected group with
 * their now/next programmes, and per-group focus memory.
 */
class PanelViewModel(
    channelDao: ChannelDao,
    private val epgRepository: EpgRepository,
    private val clock: () -> Long,
    scope: CoroutineScope,
    private val zone: TimeZone = TimeZone.getDefault(),
) {
    private val channels = channelDao.observeVisible().stateIn(scope, SharingStarted.Eagerly, emptyList())
    private val selected = MutableStateFlow(ALL_CHANNELS)
    private val instant = MutableStateFlow(clock())
    private val mutableFocusIndex = MutableStateFlow(0)
    private val focusMemory = mutableMapOf<String, Int>()

    val selectedGroup: StateFlow<String> = selected.asStateFlow()
    val focusIndex: StateFlow<Int> = mutableFocusIndex.asStateFlow()

    /** Panel header clock, "Sun, Sep 13, 2:53 PM" in blue (capture 47). */
    val clockText: StateFlow<String> =
        instant
            .map { ProgramTimes.clock(it, zone) }
            .stateIn(scope, SharingStarted.Eagerly, "")

    val groups: StateFlow<List<String>> =
        channels
            .map(PanelRows::groupNames)
            .stateIn(scope, SharingStarted.Eagerly, PanelRows.groupNames(emptyList()))

    @OptIn(ExperimentalCoroutinesApi::class)
    val rows: StateFlow<List<PanelRow>> =
        combine(channels, selected, instant) { list, group, at -> Triple(PanelRows.channelsIn(list, group), group, at) }
            .flatMapLatest { (groupChannels, group, at) ->
                epgRepository
                    .nowNext(groupChannels.mapNotNull { it.source.tvgId }, at)
                    .map { guide -> PanelRows.build(groupChannels, group, guide, at, zone) }
            }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** The airing programme title of a row — the channel menu's blue header. */
    fun nowTitleOf(channelId: Long): String? = rows.value.firstOrNull { it.channel.id == channelId }?.nowTitle

    /** Refreshes "now" and moves focus to [channelId] (the tuned/previous one). */
    fun openFocusedOn(channelId: Long?) {
        instant.value = clock()
        channelId?.let(::focusChannel)
    }

    fun selectGroup(group: String) {
        if (group == selected.value) return
        selected.value = group
        mutableFocusIndex.value = focusMemory[group] ?: 0
    }

    fun onRowFocused(index: Int) {
        focusMemory[selected.value] = index
        mutableFocusIndex.value = index
    }

    private fun focusChannel(channelId: Long) {
        val index = PanelRows.channelsIn(channels.value, selected.value).indexOfFirst { it.id == channelId }
        if (index >= 0) {
            onRowFocused(index)
        } else {
            selected.value = ALL_CHANNELS
            onRowFocused(channels.value.indexOfFirst { it.id == channelId }.coerceAtLeast(0))
        }
    }

    companion object {
        const val FAVORITES = "Favorites"
        const val ALL_CHANNELS = "All channels"
    }
}
