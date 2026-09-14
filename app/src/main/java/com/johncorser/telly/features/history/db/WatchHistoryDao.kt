package com.johncorser.telly.features.history.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Watch-history queries: newest-first keys, per-channel dedupe, capped size. */
@Dao
interface WatchHistoryDao {
    /** Re-watching a channel replaces its row, keeping only the most recent. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(event: WatchHistoryEntity)

    /** Channel identity keys, most recently watched first. */
    @Query("SELECT channelKey FROM watch_history ORDER BY watchedAtMs DESC, channelKey")
    fun observeKeys(): Flow<List<String>>

    /** Drops everything older than the [cap] most recent watches. */
    @Query(
        "DELETE FROM watch_history WHERE channelKey NOT IN " +
            "(SELECT channelKey FROM watch_history ORDER BY watchedAtMs DESC, channelKey LIMIT :cap)",
    )
    suspend fun trimTo(cap: Int)
}
