package com.johncorser.telly.features.history.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One watch event per channel. The key is the playlist-refresh-stable
 * channel identity (ChannelImporter.identityOf) — Room channel row ids are
 * reassigned on every import — and the primary key dedupes per channel:
 * re-watching a channel just moves it to the top.
 */
@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val channelKey: String,
    val watchedAtMs: Long,
)
