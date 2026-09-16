package com.johncorser.telly.features.playlist.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** One row of the guide's group list: a group name plus its channel count. */
data class ChannelGroupCount(
    val groupTitle: String?,
    val channelCount: Int,
)

/**
 * Persistence for channels; queries shaped for the guide/group UI slices.
 * The per-channel Channel-options queries (fresh-row reads, EPG offsets,
 * the row update) are inherited from [ChannelOptionsDao].
 */
@Dao
interface ChannelDao : ChannelOptionsDao {
    @Query("SELECT * FROM channels WHERE playlistId = :playlistId ORDER BY number")
    fun observeForPlaylist(playlistId: Long): Flow<List<ChannelEntity>>

    @Query(
        "SELECT * FROM channels WHERE playlistId = :playlistId AND groupTitle = :groupTitle " +
            "AND hidden = 0 ORDER BY sortIndex, number",
    )
    fun observeByGroup(
        playlistId: Long,
        groupTitle: String,
    ): Flow<List<ChannelEntity>>

    @Query(
        "SELECT groupTitle, COUNT(*) AS channelCount FROM channels " +
            "WHERE playlistId = :playlistId AND hidden = 0 " +
            "GROUP BY groupTitle ORDER BY MIN(sortIndex)",
    )
    fun observeGroups(playlistId: Long): Flow<List<ChannelGroupCount>>

    /**
     * All visible channels across playlists in "All channels" zap order:
     * the per-channel sort index ("Reorder channels" swaps neighbours'),
     * which starts as the import order; `number` breaks cross-playlist ties.
     */
    @Query("SELECT * FROM channels WHERE hidden = 0 ORDER BY sortIndex, number")
    fun observeVisible(): Flow<List<ChannelEntity>>

    @Query("SELECT COUNT(*) FROM channels")
    suspend fun totalCount(): Int

    @Query("SELECT COUNT(DISTINCT groupTitle) FROM channels WHERE hidden = 0")
    suspend fun totalGroupCount(): Int

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId ORDER BY sortIndex")
    suspend fun forPlaylist(playlistId: Long): List<ChannelEntity>

    @Query("DELETE FROM channels WHERE playlistId = :playlistId")
    suspend fun deleteForPlaylist(playlistId: Long)

    @Insert
    suspend fun insertAll(channels: List<ChannelEntity>)
}
