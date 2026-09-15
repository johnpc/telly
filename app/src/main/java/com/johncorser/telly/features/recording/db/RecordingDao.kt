package com.johncorser.telly.features.recording.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** DVR queries: the library list, scheduler lookups and status updates. */
@Dao
interface RecordingDao {
    @Insert
    suspend fun insert(recording: RecordingEntity): Long

    /** The library: newest start first (in-progress entries sort on top). */
    @Query("SELECT * FROM recordings ORDER BY startMs DESC, id DESC")
    fun observeAll(): Flow<List<RecordingEntity>>

    /** The scheduler's watch list, soonest start first. */
    @Query("SELECT * FROM recordings WHERE status = 'SCHEDULED' ORDER BY startMs, id")
    fun observeScheduled(): Flow<List<RecordingEntity>>

    @Query("SELECT * FROM recordings WHERE id = :id")
    suspend fun byId(id: Long): RecordingEntity?

    @Query("SELECT * FROM recordings WHERE status = :status ORDER BY id")
    suspend fun byStatus(status: String): List<RecordingEntity>

    @Query(
        "SELECT COUNT(*) FROM recordings WHERE channelKey = :channelKey " +
            "AND startMs = :startMs AND status IN ('SCHEDULED', 'RECORDING')",
    )
    suspend fun countPlanned(
        channelKey: String,
        startMs: Long,
    ): Int

    @Query("UPDATE recordings SET status = :status, endMs = :endMs, sizeBytes = :sizeBytes WHERE id = :id")
    suspend fun finish(
        id: Long,
        status: String,
        endMs: Long,
        sizeBytes: Long,
    )

    @Query("UPDATE recordings SET status = :status, startMs = :startMs WHERE id = :id")
    suspend fun markStarted(
        id: Long,
        status: String,
        startMs: Long,
    )

    @Query("DELETE FROM recordings WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM recordings")
    suspend fun deleteAll()
}
