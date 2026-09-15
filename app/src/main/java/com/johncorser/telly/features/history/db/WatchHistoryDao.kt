package com.johncorser.telly.features.history.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Watch-history queries: newest-first events, per-channel dedupe, capped size. */
@Dao
interface WatchHistoryDao {
    /** Re-watching a channel replaces its row, keeping only the most recent. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(event: WatchHistoryEntity)

    /** Watch events, most recently watched first. */
    @Query("SELECT * FROM watch_history ORDER BY watchedAtMs DESC, channelKey")
    fun observeEvents(): Flow<List<WatchHistoryEntity>>

    /** Drops everything older than the [cap] most recent watches. */
    @Query(
        "DELETE FROM watch_history WHERE channelKey NOT IN " +
            "(SELECT channelKey FROM watch_history ORDER BY watchedAtMs DESC, channelKey LIMIT :cap)",
    )
    suspend fun trimTo(cap: Int)

    /** The History screen's clear-all and the info-row Clear card. */
    @Query("DELETE FROM watch_history")
    suspend fun clear()
}
