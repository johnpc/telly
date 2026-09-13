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
}
