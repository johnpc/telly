package com.johncorser.telly.features.groups.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One user-created channel group (ux-spec §5 premium group management).
 * Custom groups live NEXT TO the playlist's group-title groups: the guide
 * and panel group columns append them after the playlist groups, ordered
 * by [sortIndex] (creation order until a reorder slice ships).
 */
@Entity(tableName = "custom_groups")
data class CustomGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sortIndex: Int,
)

/**
 * Membership of one channel in one custom group. Channels are referenced
 * by [ChannelImporter.identityOf][com.johncorser.telly.features.playlist.ChannelImporter.identityOf]
 * (tvg-id, falling back to stream URL + name) — NOT by row id — so
 * membership survives playlist refreshes, which replace all channel rows,
 * exactly like the favorite/hidden flags do.
 */
@Entity(tableName = "custom_group_members", primaryKeys = ["groupId", "channelKey"])
data class CustomGroupMemberEntity(
    val groupId: Long,
    val channelKey: String,
)
