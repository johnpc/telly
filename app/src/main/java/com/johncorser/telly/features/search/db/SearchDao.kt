package com.johncorser.telly.features.search.db

import androidx.room.Dao
import androidx.room.Query
import com.johncorser.telly.features.epg.db.ProgramEntity
import com.johncorser.telly.features.playlist.db.ChannelEntity

/**
 * The search slice's Room queries (catalogue §4). LIKE patterns arrive
 * pre-escaped from SearchQuery (ESCAPE '\'), so user input can never act
 * as a wildcard.
 */
@Dao
interface SearchDao {
    /** Channels by name substring or number prefix, in name order (live 5.2.0, tm-02). */
    @Query(
        "SELECT * FROM channels WHERE hidden = 0 AND (name LIKE :nameLike ESCAPE '\\' " +
            "OR CAST(number AS TEXT) LIKE :numberLike ESCAPE '\\') ORDER BY name COLLATE NOCASE, number",
    )
    suspend fun channels(
        nameLike: String,
        numberLike: String,
    ): List<ChannelEntity>

    /**
     * Programmes by title substring, still airing or upcoming, soonest
     * first — the LIMIT keeps the query bounded (the reference cap is not
     * capturable), so it caps the soonest airings overall; the builder then
     * regroups them per channel for the master–detail Programs section.
     */
    @Query(
        "SELECT * FROM programs WHERE endMs > :atMs AND title LIKE :titleLike ESCAPE '\\' " +
            "ORDER BY startMs, channelTvgId LIMIT :limit",
    )
    suspend fun programs(
        titleLike: String,
        atMs: Long,
        limit: Int,
    ): List<ProgramEntity>
}
