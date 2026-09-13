package com.johncorser.telly.features.playlist

import kotlinx.coroutines.flow.StateFlow

/** A playlist the user added, keyed by the URL it was loaded from. */
data class StoredPlaylist(
    val sourceUrl: String,
    val playlist: M3uPlaylist,
)

/**
 * Storage boundary for user playlists. This slice ships an in-memory
 * implementation; a Room-backed one replaces it in a later slice.
 */
interface PlaylistRepository {
    /** All stored playlists, in insertion order. */
    val playlists: StateFlow<List<StoredPlaylist>>

    /** Adds [playlist]; re-adding the same [sourceUrl] replaces the old copy. */
    suspend fun add(
        sourceUrl: String,
        playlist: M3uPlaylist,
    )
}
