package com.johncorser.telly.features.playlist

import kotlinx.coroutines.flow.Flow

/** A playlist the user added, keyed by the URL it was loaded from. */
data class StoredPlaylist(
    val sourceUrl: String,
    val playlist: M3uPlaylist,
)

/**
 * Storage boundary for user playlists. The Room-backed implementation is
 * the production one; the in-memory one remains for fast JVM tests.
 */
interface PlaylistRepository {
    /** All stored playlists, in insertion order. */
    val playlists: Flow<List<StoredPlaylist>>

    /** Adds [playlist]; re-adding the same [sourceUrl] replaces the old copy. */
    suspend fun add(
        sourceUrl: String,
        playlist: M3uPlaylist,
    )
}
