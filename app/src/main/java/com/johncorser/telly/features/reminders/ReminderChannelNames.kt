package com.johncorser.telly.features.reminders

import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/** Resolves a due reminder's channel id to its display name for the popup. */
class ReminderChannelNames(
    private val channels: Flow<List<ChannelEntity>>,
) {
    suspend fun nameOf(channelId: Long): String =
        channels.first().firstOrNull { it.id == channelId }?.source?.name.orEmpty()
}
