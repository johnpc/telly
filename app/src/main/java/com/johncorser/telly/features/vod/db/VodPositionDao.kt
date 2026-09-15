package com.johncorser.telly.features.vod.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Persistence for VOD resume positions ("Continue watching"). */
@Dao
interface VodPositionDao {
    /** All stored positions; the browser joins them onto its item cards. */
    @Query("SELECT * FROM vod_positions")
    fun observeAll(): Flow<List<VodPositionEntity>>

    @Query("SELECT * FROM vod_positions WHERE itemKey = :itemKey")
    suspend fun byKey(itemKey: String): VodPositionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(position: VodPositionEntity)

    @Query("DELETE FROM vod_positions WHERE itemKey = :itemKey")
    suspend fun delete(itemKey: String)

    /** Settings -> Other -> VOD -> Clear playback positions. */
    @Query("DELETE FROM vod_positions")
    suspend fun clearAll()
}
