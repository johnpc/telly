package com.johncorser.telly.features.playlist

import com.johncorser.telly.features.playlist.db.ChannelEntity

/**
 * Manage groups: drops the channels of disabled playlist groups from the
 * visible-channel feed the guide/panel/search/zap slices read. Ungrouped
 * channels and channels of unknown playlists always stay visible.
 */
object PlaylistGroupFilter {
    fun visible(
        channels: List<ChannelEntity>,
        playlistUrlById: Map<Long, String>,
        groupEnabled: (playlistUrl: String, group: String) -> Boolean,
    ): List<ChannelEntity> =
        channels.filter { channel ->
            val group = channel.source.groupTitle ?: return@filter true
            val playlistUrl = playlistUrlById[channel.playlistId] ?: return@filter true
            groupEnabled(playlistUrl, group)
        }
}
