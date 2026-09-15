package com.johncorser.telly.features.epg.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Persistence for custom EPG sources, in the order they were added. */
@Dao
interface EpgSourceDao {
    @Query("SELECT * FROM epg_sources ORDER BY addedAtMs, id")
    fun observeAll(): Flow<List<EpgSourceEntity>>

    @Query("SELECT * FROM epg_sources WHERE playlistUrl = :playlistUrl ORDER BY addedAtMs, id")
    suspend fun forPlaylist(playlistUrl: String): List<EpgSourceEntity>

    /** Re-adding the same URL for a playlist replaces the old row. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(source: EpgSourceEntity): Long

    @Query("UPDATE epg_sources SET url = :url WHERE id = :id")
    suspend fun setUrl(
        id: Long,
        url: String,
    )

    @Query("DELETE FROM epg_sources WHERE id = :id")
    suspend fun delete(id: Long)

    /** Follows a playlist URL edit: re-keys its custom sources in place. */
    @Query("UPDATE epg_sources SET playlistUrl = :newUrl WHERE playlistUrl = :oldUrl")
    suspend fun rekeyPlaylist(
        oldUrl: String,
        newUrl: String,
    )
}
