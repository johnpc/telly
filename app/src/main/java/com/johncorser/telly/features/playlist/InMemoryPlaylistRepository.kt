package com.johncorser.telly.features.playlist

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Process-lifetime playlist store, kept for fast JVM tests. */
class InMemoryPlaylistRepository : PlaylistRepository {
    private val mutablePlaylists = MutableStateFlow(emptyList<StoredPlaylist>())

    override val playlists: StateFlow<List<StoredPlaylist>> = mutablePlaylists.asStateFlow()

    override suspend fun add(
        sourceUrl: String,
        playlist: M3uPlaylist,
        name: String?,
    ) {
        mutablePlaylists.update { current ->
            val existingName = current.firstOrNull { it.sourceUrl == sourceUrl }?.name
            current.filterNot { it.sourceUrl == sourceUrl } +
                StoredPlaylist(sourceUrl, playlist, name ?: existingName)
        }
    }

    override suspend fun rename(
        sourceUrl: String,
        name: String,
    ) {
        mutablePlaylists.update { current ->
            current.map { if (it.sourceUrl == sourceUrl) it.copy(name = name) else it }
        }
    }

    override suspend fun delete(sourceUrl: String) {
        mutablePlaylists.update { current -> current.filterNot { it.sourceUrl == sourceUrl } }
    }
}
