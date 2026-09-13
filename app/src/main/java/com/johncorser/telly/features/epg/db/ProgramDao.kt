package com.johncorser.telly.features.epg.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Persistence for guide programmes, shaped for the guide grid's window. */
@Dao
interface ProgramDao {
    /** Programmes overlapping [fromMs, toMs) for the given channels. */
    @Query(
        "SELECT * FROM programs WHERE channelTvgId IN (:tvgIds) " +
            "AND endMs > :fromMs AND startMs < :toMs ORDER BY channelTvgId, startMs",
    )
    fun observeWindow(
        tvgIds: List<String>,
        fromMs: Long,
        toMs: Long,
    ): Flow<List<ProgramEntity>>

    /** Everything still airing or upcoming at [atMs]; feeds now/next. */
    @Query(
        "SELECT * FROM programs WHERE channelTvgId IN (:tvgIds) " +
            "AND endMs > :atMs ORDER BY channelTvgId, startMs",
    )
    fun observeAiringOrUpcoming(
        tvgIds: List<String>,
        atMs: Long,
    ): Flow<List<ProgramEntity>>

    /** Upsert with replace semantics via the unique (channelTvgId, startMs) index. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(programs: List<ProgramEntity>)

    @Query("DELETE FROM programs WHERE channelTvgId IN (:tvgIds)")
    suspend fun deleteFor(tvgIds: List<String>)

    /** Trims history; mirrors TiviMate's "past days to keep EPG" setting. */
    @Query("DELETE FROM programs WHERE endMs < :beforeMs")
    suspend fun deleteEndedBefore(beforeMs: Long)

    @Query("SELECT COUNT(*) FROM programs")
    suspend fun count(): Int
}
