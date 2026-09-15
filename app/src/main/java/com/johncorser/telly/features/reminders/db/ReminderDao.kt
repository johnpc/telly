package com.johncorser.telly.features.reminders.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Persistence for programme reminders. Rows are pruned by the engine once
 * fired (the TiviMate popup appears once) or once their programme ends, so
 * the observed list is the upcoming schedule.
 */
@Dao
interface ReminderDao {
    /** Re-reminding the same programme replaces its row (unique index). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(reminder: ReminderEntity): Long

    /** Upcoming reminders in air order (fired/ended rows are deleted). */
    @Query("SELECT * FROM reminders ORDER BY startMs, id")
    fun observeUpcoming(): Flow<List<ReminderEntity>>

    /** The reminder set for one programme, if any (dropdown toggle). */
    @Query("SELECT * FROM reminders WHERE channelId = :channelId AND startMs = :startMs AND title = :title LIMIT 1")
    suspend fun find(
        channelId: Long,
        startMs: Long,
        title: String,
    ): ReminderEntity?

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun delete(id: Long)
}
