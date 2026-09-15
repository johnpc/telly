package com.johncorser.telly.features.vod.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.johncorser.telly.features.playlist.db.PlaylistEntity

/**
 * One VOD "Movies" item: a playlist entry whose stream URL carries a
 * video-file extension (TiviMate classification, ux-spec §VOD). Items are
 * split out of the channel import so they never appear in the guide;
 * `itemKey` is the refresh-stable identity resume positions are keyed by.
 */
@Entity(
    tableName = "vod_items",
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["playlistId"])],
)
data class VodItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long,
    val sortIndex: Int,
    val itemKey: String,
    val name: String,
    val groupTitle: String? = null,
    val logoUrl: String? = null,
    val streamUrl: String,
)
