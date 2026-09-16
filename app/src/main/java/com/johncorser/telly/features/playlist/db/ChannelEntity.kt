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
    /** PIN-gated to tune (ux-spec: blocked channels stay listed with a lock, v9). */
    @ColumnInfo(defaultValue = "0") val blocked: Boolean = false,
)

/**
 * Per-channel "Channel options" overrides (§41 pane): all default to
 * "follow the playlist/global" and survive playlist refreshes like
 * [ChannelFlags] (carried over by [com.johncorser.telly.features.playlist.ChannelImporter]).
 */
data class ChannelOverrides(
    /** Custom display name; null/blank = the playlist name. */
    val customName: String? = null,
    /** "Hardware"/"Software"; null = the global Playback setting. */
    val audioDecoder: String? = null,
    val videoDecoder: String? = null,
    /** Shifts this channel's EPG programme times for display. */
    @ColumnInfo(defaultValue = "0") val epgOffsetMinutes: Int = 0,
    /** "On"/"Off"; null = the global "Use external player" setting. */
    val externalPlayer: String? = null,
)

/** What every channel-facing surface renders: the custom name when set. */
val ChannelEntity.displayName: String
    get() = overrides.customName?.takeIf { it.isNotBlank() } ?: source.name

/**
 * Catch-up capability as declared on the channel's `#EXTINF` line
 * (`catchup` / `catchup-source` / `catchup-days`); re-imported from the
 * playlist on every refresh like the rest of [ChannelSource].
 */
data class ChannelCatchup(
    val catchupType: String? = null,
    val catchupSource: String? = null,
    val catchupDays: Int? = null,
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
    @Embedded val catchup: ChannelCatchup = ChannelCatchup(),
    @Embedded val overrides: ChannelOverrides = ChannelOverrides(),
)
