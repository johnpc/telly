package com.johncorser.telly.features.playlist

import kotlinx.coroutines.flow.Flow

/** A playlist the user added, keyed by the URL it was loaded from. */
data class StoredPlaylist(
    val sourceUrl: String,
    val playlist: M3uPlaylist,
    val name: String? = null,
)

/**
 * Storage boundary for user playlists. The Room-backed implementation is
 * the production one; the in-memory one remains for fast JVM tests.
 */
interface PlaylistRepository {
    /** All stored playlists, in insertion order. */
    val playlists: Flow<List<StoredPlaylist>>

    /**
     * Adds [playlist]; re-adding the same [sourceUrl] replaces the old copy.
     * A null [name] keeps the existing name (or derives a default).
     */
    suspend fun add(
        sourceUrl: String,
        playlist: M3uPlaylist,
        name: String? = null,
    )

    /** Renames the playlist stored under [sourceUrl] (settings: rename). */
    suspend fun rename(
        sourceUrl: String,
        name: String,
    )

    /** Deletes the playlist stored under [sourceUrl] and its channels. */
    suspend fun delete(sourceUrl: String)
}
