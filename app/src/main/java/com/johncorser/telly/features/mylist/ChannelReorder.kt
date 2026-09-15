package com.johncorser.telly.features.mylist

import com.johncorser.telly.features.playlist.db.ChannelEntity

/**
 * Pure ordering math for Manage Favorites and Reorder channels. Favorites
 * order lives in the refresh-surviving `favoriteOrder` flag (ties keep the
 * base zap order — the sort is stable, so untouched favorites stay put);
 * group order swaps the neighbours' `sortIndex`, which every channel query
 * orders by. Both return only the rows that must be persisted.
 */
object ChannelReorder {
    /** The Favorites group in its managed order (guide/panel + editors). */
    fun favorites(channels: List<ChannelEntity>): List<ChannelEntity> =
        channels.filter { it.flags.favorite }.sortedBy { it.flags.favoriteOrder }

    /** Manage Favorites list: favorites first (in order), then the rest. */
    fun editorRows(channels: List<ChannelEntity>): List<ChannelEntity> =
        favorites(channels) + channels.filterNot { it.flags.favorite }

    /** OK toggles: adding appends to the favorites order, removing keeps it. */
    fun toggled(
        channels: List<ChannelEntity>,
        channel: ChannelEntity,
    ): ChannelEntity {
        val nextOrder = (favorites(channels).maxOfOrNull { it.flags.favoriteOrder } ?: -1) + 1
        val flags =
            if (channel.flags.favorite) {
                channel.flags.copy(favorite = false)
            } else {
                channel.flags.copy(favorite = true, favoriteOrder = nextOrder)
            }
        return channel.copy(flags = flags)
    }

    /** LEFT/RIGHT on a favorite moves it within the favorites order. */
    fun moveFavorite(
        channels: List<ChannelEntity>,
        channelId: Long,
        delta: Int,
    ): List<ChannelEntity> {
        val reordered = swap(favorites(channels), channelId, delta) ?: return emptyList()
        return reordered
            .mapIndexed { index, channel -> channel.copy(flags = channel.flags.copy(favoriteOrder = index)) }
            .filterIndexed { index, channel -> reordered[index] != channel }
    }

    /** LEFT/RIGHT in a group swaps the two neighbours' sort indices. */
    fun moveInGroup(
        ordered: List<ChannelEntity>,
        channelId: Long,
        delta: Int,
    ): List<ChannelEntity> {
        val index = ordered.indexOfFirst { it.id == channelId }
        val other = index + delta
        if (index < 0 || other !in ordered.indices) return emptyList()
        val a = ordered[index]
        val b = ordered[other]
        return listOf(a.copy(sortIndex = b.sortIndex), b.copy(sortIndex = a.sortIndex))
    }

    private fun swap(
        ordered: List<ChannelEntity>,
        channelId: Long,
        delta: Int,
    ): List<ChannelEntity>? {
        val index = ordered.indexOfFirst { it.id == channelId }
        val other = index + delta
        if (index < 0 || other !in ordered.indices) return null
        val out = ordered.toMutableList()
        out[index] = ordered[other]
        out[other] = ordered[index]
        return out
    }
}
