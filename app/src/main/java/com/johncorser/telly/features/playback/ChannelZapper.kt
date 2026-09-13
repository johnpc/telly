package com.johncorser.telly.features.playback

import com.johncorser.telly.features.playlist.db.ChannelEntity

/** Pure channel-adjacency math: restore-on-start and wrap-around zapping. */
object ChannelZapper {
    /** The last-watched channel if it still exists, else the first channel. */
    fun restore(
        channels: List<ChannelEntity>,
        lastChannelId: Long?,
    ): ChannelEntity? = channels.firstOrNull { it.id == lastChannelId } ?: channels.firstOrNull()

    /**
     * The channel [delta] steps away from [current] in "All channels" order,
     * wrapping at both ends (UP from channel 1 lands on channel 30 and back —
     * capture 36 confirms 1 -> 30 wrapping).
     */
    fun neighbour(
        channels: List<ChannelEntity>,
        current: ChannelEntity?,
        delta: Int,
    ): ChannelEntity? {
        if (channels.isEmpty()) return null
        val index = channels.indexOfFirst { it.id == current?.id }
        if (index < 0) return channels.first()
        return channels[(index + delta).mod(channels.size)]
    }
}
