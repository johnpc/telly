package com.johncorser.telly.features.multiview

import com.johncorser.telly.features.epg.EpgRepository
import com.johncorser.telly.features.panel.PanelRow
import com.johncorser.telly.features.panel.PanelViewModel
import com.johncorser.telly.features.playlist.db.ChannelDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.TimeZone

/**
 * The channel picker every pane-menu row opens (multiview-round 05/08/10):
 * the panel's channel list on the left (reused [PanelViewModel] rows — the
 * pane's current channel carries the play arrow), the focused channel's
 * schedule in the middle and its airing programme's detail card top-right.
 */
class MultiviewPicker(
    channelDao: ChannelDao,
    private val epgRepository: EpgRepository,
    private val clock: () -> Long,
    scope: CoroutineScope,
    private val zone: TimeZone,
) {
    /** Channel rows + focus machinery, shared 1:1 with the channel panel. */
    val panel = PanelViewModel(channelDao, epgRepository, clock, scope, zone)

    /** The focused row feeds the schedule pane and the detail card. */
    val focusedRow: StateFlow<PanelRow?> =
        combine(panel.rows, panel.focusIndex) { rows, index -> rows.getOrNull(index) }
            .stateIn(scope, SharingStarted.Eagerly, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val schedule: StateFlow<List<MultiviewScheduleRow>> =
        focusedRow
            .map { it?.channel?.source?.tvgId }
            .distinctUntilChanged()
            .flatMapLatest(::scheduleFor)
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** Opening refreshes "now" and focuses the pane's current channel. */
    fun open(currentChannelId: Long?) = panel.openFocusedOn(currentChannelId)

    private fun scheduleFor(tvgId: String?): Flow<List<MultiviewScheduleRow>> {
        if (tvgId == null) return flowOf(emptyList())
        val at = clock()
        return epgRepository
            .programsFor(listOf(tvgId), at - MultiviewSchedule.PAST_MS, at + MultiviewSchedule.FUTURE_MS)
            .map { MultiviewSchedule.build(it, at, zone) }
    }
}
