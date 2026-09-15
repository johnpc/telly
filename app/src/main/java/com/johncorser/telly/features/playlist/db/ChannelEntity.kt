package com.johncorser.telly.features.playlist.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Where a channel comes from and how it is labelled in the playlist. */
data class ChannelSource(
    val name: String,
    val groupTitle: String? = null,
    val logoUrl: String? = null,
    val streamUrl: String,
    val tvgId: String? = null,
)

/** Per-channel user state that must survive playlist refreshes. */
data class ChannelFlags(
    val favorite: Boolean = false,
    val hidden: Boolean = false,
    /** Manage-Favorites position; ties keep the base zap order (v5). */
    @ColumnInfo(defaultValue = "0") val favoriteOrder: Int = 0,
)

/**
 * One channel row. `number` is the TiviMate-style sequential channel number
 * assigned from playlist order on import; `sortIndex` preserves that order
 * independently of future renumbering.
 */
@Entity(
    tableName = "channels",
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
data class ChannelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long,
    val number: Int,
    val sortIndex: Int,
    @Embedded val source: ChannelSource,
    @Embedded val flags: ChannelFlags = ChannelFlags(),
)
