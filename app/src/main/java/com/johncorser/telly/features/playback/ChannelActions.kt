package com.johncorser.telly.features.playback

import com.johncorser.telly.features.playlist.db.ChannelDao
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * The two context-menu actions that are real this slice: favorite toggling
 * and hiding, persisted straight through [ChannelDao] so they survive
 * restarts and playlist refreshes (ChannelImporter carries the flags over).
 */
class ChannelActions(
    private val channelDao: ChannelDao,
    private val scope: CoroutineScope,
) {
    fun toggleFavorite(channel: ChannelEntity) {
        update(channel.copy(flags = channel.flags.copy(favorite = !channel.flags.favorite)))
    }

    fun hide(channel: ChannelEntity) {
        update(channel.copy(flags = channel.flags.copy(hidden = true)))
    }

    /** Block/unblock; blocked channels stay listed but PIN-gate tuning. */
    fun toggleBlocked(channel: ChannelEntity) {
        update(channel.copy(flags = channel.flags.copy(blocked = !channel.flags.blocked)))
    }

    private fun update(channel: ChannelEntity) {
        scope.launch { channelDao.update(channel) }
    }
}
