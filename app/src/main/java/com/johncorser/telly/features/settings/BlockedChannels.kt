package com.johncorser.telly.features.settings

import com.johncorser.telly.features.playlist.db.ChannelDao
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The parental "Blocked channels" pane's data seam: the live blocked list
 * (derived from the visible channels — a hidden channel is invisible
 * everywhere, so it has nothing to unblock) and the OK-to-unblock action,
 * persisted straight through [ChannelDao] (ChannelImporter carries the
 * flag across playlist refreshes).
 */
class BlockedChannels(
    private val channelDao: ChannelDao,
) {
    val channels: Flow<List<ChannelEntity>> =
        channelDao.observeVisible().map { list -> list.filter { it.flags.blocked } }

    suspend fun unblock(channel: ChannelEntity) {
        channelDao.update(channel.copy(flags = channel.flags.copy(blocked = false)))
    }
}
