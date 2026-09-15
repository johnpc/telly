package com.johncorser.telly.features.vod.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Persistence for VOD items; queries shaped for the Movies browser. */
@Dao
interface VodItemDao {
    /** Every imported VOD item in playlist order (categories derive from it). */
    @Query("SELECT * FROM vod_items ORDER BY sortIndex")
    fun observeAll(): Flow<List<VodItemEntity>>

    @Query("SELECT * FROM vod_items WHERE itemKey = :itemKey LIMIT 1")
    suspend fun byKey(itemKey: String): VodItemEntity?

    @Query("SELECT COUNT(*) FROM vod_items")
    suspend fun totalCount(): Int

    @Query("DELETE FROM vod_items WHERE playlistId = :playlistId")
    suspend fun deleteForPlaylist(playlistId: Long)

    @Insert
    suspend fun insertAll(items: List<VodItemEntity>)
}
