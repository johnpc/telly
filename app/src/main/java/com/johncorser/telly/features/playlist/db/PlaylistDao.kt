package com.johncorser.telly.features.playlist.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Persistence for user playlists. */
@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY id")
    fun observeAll(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists ORDER BY id")
    suspend fun all(): List<PlaylistEntity>

    @Query("SELECT * FROM playlists WHERE url = :url")
    suspend fun byUrl(url: String): PlaylistEntity?

    /** Insert-or-replace keyed by the unique `url` index; returns the row id. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET epgLastUpdatedMs = :updatedMs WHERE id = :id")
    suspend fun markEpgUpdated(
        id: Long,
        updatedMs: Long,
    )

    @Query("UPDATE playlists SET name = :name WHERE url = :url")
    suspend fun rename(
        url: String,
        name: String,
    )

    /** Re-keys the playlist in place (URL edit); the row id and channels survive. */
    @Query("UPDATE playlists SET url = :newUrl WHERE url = :oldUrl")
    suspend fun updateUrl(
        oldUrl: String,
        newUrl: String,
    )

    /**
     * The URL of the playlist owning the channel OR VOD item at [streamUrl]
     * (VOD playback sends the same per-playlist User-Agent as live streams).
     * Blocking on purpose: the stream User-Agent resolver runs on a player
     * loader thread.
     */
    @Query(
        "SELECT url FROM playlists WHERE id = " +
            "(SELECT playlistId FROM channels WHERE streamUrl = :streamUrl " +
            "UNION ALL SELECT playlistId FROM vod_items WHERE streamUrl = :streamUrl LIMIT 1)",
    )
    fun playlistUrlForStream(streamUrl: String): String?

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun delete(id: Long)
}
