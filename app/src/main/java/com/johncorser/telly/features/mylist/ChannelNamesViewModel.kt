package com.johncorser.telly.features.mylist

import com.johncorser.telly.features.guide.ChannelOptionsStore
import com.johncorser.telly.features.playlist.db.ChannelDao
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * The Channel-options pane's bulk "Channel names editor": every visible
 * channel in zap order; OK on a row opens the rename dialog, IME Done
 * persists through the same [ChannelOptionsStore] the pane uses (blank
 * restores the playlist name).
 */
class ChannelNamesViewModel(
    channelDao: ChannelDao,
    scope: CoroutineScope,
) {
    private val store = ChannelOptionsStore(channelDao, scope)

    val rows: StateFlow<List<ChannelEntity>> =
        channelDao.observeVisible().stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val mutableEditing = MutableStateFlow<ChannelEntity?>(null)

    /** The channel whose rename dialog is open (null = the list). */
    val editing: StateFlow<ChannelEntity?> = mutableEditing.asStateFlow()

    fun edit(channel: ChannelEntity) {
        mutableEditing.value = channel
    }

    fun rename(text: String) {
        mutableEditing.value?.let { store.rename(it.id, text) }
        mutableEditing.value = null
    }

    /** BACK closes an open rename dialog first; false = nothing open. */
    fun closeDialog(): Boolean {
        val wasOpen = mutableEditing.value != null
        mutableEditing.value = null
        return wasOpen
    }
}
