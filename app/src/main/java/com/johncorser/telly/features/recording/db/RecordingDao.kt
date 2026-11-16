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

    /**
     * [from] -> [status] with the end stamp and size; returns the rows
     * changed (0 = the row was not in [from]: cancelled, deleted, already
     * finished). RECORDING -> DONE | FAILED for a finished capture,
     * SCHEDULED -> FAILED for a missed window.
     */
    @Query(
        "UPDATE recordings SET status = :status, endMs = :endMs, sizeBytes = :sizeBytes " +
            "WHERE id = :id AND status = :from",
    )
    suspend fun finish(
        id: Long,
        from: String,
        status: String,
        endMs: Long,
        sizeBytes: Long,
    ): Int

    /** SCHEDULED -> RECORDING; returns the rows changed (0 = cancelled/deleted/already started). */
    @Query("UPDATE recordings SET status = :status, startMs = :startMs WHERE id = :id AND status = 'SCHEDULED'")
    suspend fun markStarted(
        id: Long,
        status: String,
        startMs: Long,
    ): Int

    @Query("DELETE FROM recordings WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM recordings")
    suspend fun deleteAll()
}
