package com.johncorser.telly.features.mylist

import com.johncorser.telly.features.panel.PanelRows
import com.johncorser.telly.features.panel.PanelViewModel
import com.johncorser.telly.features.playlist.db.ChannelDao
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Shared state for the two channel-management screens (TiviMate premium's
 * documented favorites management, ux-spec §3): a null [group] is Manage
 * Favorites (all channels, favorites first, OK toggles), a non-null one is
 * Reorder channels within that group. Either way LEFT/RIGHT moves the
 * focused row and every change persists straight through [ChannelDao].
 */
class ChannelEditViewModel(
    private val channelDao: ChannelDao,
    private val scope: CoroutineScope,
    private val group: String? = null,
) {
    private val channels = channelDao.observeVisible().stateIn(scope, SharingStarted.Eagerly, emptyList())

    val rows: StateFlow<List<ChannelEntity>> =
        channels
            .map { list -> if (group == null) ChannelReorder.editorRows(list) else PanelRows.channelsIn(list, group) }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** OK in Manage Favorites toggles; the reorder screen keeps OK inert. */
    fun toggle(channel: ChannelEntity) {
        if (group != null) return
        persist(listOf(ChannelReorder.toggled(channels.value, channel)))
    }

    /** LEFT (-1) / RIGHT (+1) moves the focused row up/down the order. */
    fun move(
        channel: ChannelEntity,
        delta: Int,
    ) {
        val updates =
            if (group == null || group == PanelViewModel.FAVORITES) {
                ChannelReorder.moveFavorite(channels.value, channel.id, delta)
            } else {
                ChannelReorder.moveInGroup(rows.value, channel.id, delta)
            }
        persist(updates)
    }

    private fun persist(updates: List<ChannelEntity>) {
        if (updates.isEmpty()) return
        scope.launch { updates.forEach { channelDao.update(it) } }
    }
}
