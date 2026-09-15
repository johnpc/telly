package com.johncorser.telly.features.mylist.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** My-list queries: most recently added first, per-airing dedupe. */
@Dao
interface MyListDao {
    /** Saved programmes, most recently added first (the screen's order). */
    @Query("SELECT * FROM my_list ORDER BY addedAtMs DESC, channelKey, startMs")
    fun observeAll(): Flow<List<MyListEntity>>

    /** Re-saving the same airing replaces its row instead of duplicating. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: MyListEntity)

    /** The remove half of the toggle and the screen's delete affordance. */
    @Query("DELETE FROM my_list WHERE channelKey = :channelKey AND startMs = :startMs")
    suspend fun delete(
        channelKey: String,
        startMs: Long,
    )
}
