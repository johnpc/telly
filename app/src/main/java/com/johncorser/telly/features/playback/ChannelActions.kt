package com.johncorser.telly.features.playback

import com.johncorser.telly.features.mylist.MyListMenuHost
import com.johncorser.telly.features.playlist.db.ChannelDao
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * The channel context-menu actions that persist: favorite toggling and
 * hiding go straight through [ChannelDao] so they survive restarts and
 * playlist refreshes (ChannelImporter carries the flags over), and the
 * host's [myList] context runs the sheet's My-list/management rows.
 */
class ChannelActions(
    private val channelDao: ChannelDao,
    private val scope: CoroutineScope,
    /** Null only where a host has no My-list wiring (defensive default). */
    val myList: MyListMenuHost? = null,
) {
    fun toggleFavorite(channel: ChannelEntity) {
        update(channel.copy(flags = channel.flags.copy(favorite = !channel.flags.favorite)))
    }

    fun hide(channel: ChannelEntity) {
        update(channel.copy(flags = channel.flags.copy(hidden = true)))
    }

    private fun update(channel: ChannelEntity) {
        scope.launch { channelDao.update(channel) }
    }
}
