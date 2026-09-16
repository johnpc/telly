package com.johncorser.telly.features.playlist.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * A channel's per-channel EPG display offset ("EPG time offset" row),
 * keyed by its EFFECTIVE EPG id (the Assign-EPG remap when set, else the
 * playlist tvg-id) — the same id every lookup passes, so remap and offset
 * compose: remap first, then the shift.
 */
data class TvgOffset(
    val tvgId: String?,
    val epgOffsetMinutes: Int,
)

/**
 * Persistence behind the "Channel options" pane's per-channel overrides;
 * [ChannelDao] inherits these, so one Room DAO serves both surfaces.
 */
@Dao
interface ChannelOptionsDao {
    /** One channel by row id — Channel-options edits mutate a fresh copy. */
    @Query("SELECT * FROM channels WHERE id = :id")
    suspend fun byId(id: Long): ChannelEntity?

    /** The Channel-options pane re-renders live off this row. */
    @Query("SELECT * FROM channels WHERE id = :id")
    fun observeById(id: Long): Flow<ChannelEntity?>

    /** The non-zero per-channel EPG offsets the lookup layer applies. */
    @Query(
        "SELECT COALESCE(epgOverride, tvgId) AS tvgId, epgOffsetMinutes FROM channels " +
            "WHERE epgOffsetMinutes != 0 AND COALESCE(epgOverride, tvgId) IS NOT NULL",
    )
    fun observeEpgOffsets(): Flow<List<TvgOffset>>

    @Update
    suspend fun update(channel: ChannelEntity)
}
