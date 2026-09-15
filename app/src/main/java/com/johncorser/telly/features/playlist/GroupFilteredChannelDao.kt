package com.johncorser.telly.features.playlist

import com.johncorser.telly.features.playlist.db.ChannelDao
import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.playlist.db.PlaylistEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * The channel-query seam Manage groups filters at: every consumer of the
 * visible-channel feed (guide, panel, search, zap order, history,
 * multiview) receives this wrapper instead of the raw Room DAO, so
 * channels of disabled playlist groups disappear everywhere at once and
 * re-appear live when the group toggle flips ([settingsChanges]).
 */
class GroupFilteredChannelDao(
    private val delegate: ChannelDao,
    private val playlists: Flow<List<PlaylistEntity>>,
    private val settingsChanges: Flow<Map<String, String>>,
    private val groupEnabled: (playlistUrl: String, group: String) -> Boolean,
) : ChannelDao by delegate {
    override fun observeVisible(): Flow<List<ChannelEntity>> =
        combine(delegate.observeVisible(), playlists, settingsChanges) { channels, lists, _ ->
            PlaylistGroupFilter.visible(channels, lists.associate { it.id to it.url }, groupEnabled)
        }
}
