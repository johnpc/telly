package com.johncorser.telly.features.mylist.db

import androidx.room.Entity

/**
 * One saved programme ("Add to My list", capture 27's dropdown row). The
 * channel key is the playlist-refresh-stable identity
 * (ChannelImporter.identityOf) — Room channel row ids are reassigned on
 * every import — and the composite key dedupes per airing: saving the same
 * programme again is the remove half of the toggle, never a duplicate.
 */
@Entity(tableName = "my_list", primaryKeys = ["channelKey", "startMs"])
data class MyListEntity(
    val channelKey: String,
    val startMs: Long,
    val endMs: Long,
    val title: String,
    val description: String?,
    val addedAtMs: Long,
)
