package com.johncorser.telly.features.playlist.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One user-added playlist. `url` is the identity a re-add replaces on;
 * `lastUpdatedMs` / `epgLastUpdatedMs` feed the RefreshScheduler policy
 * (0 means "never", i.e. always due). `epgFailedAtMs` (v13) is the last EPG
 * attempt that fetched nothing — [com.johncorser.telly.features.epg.EpgFreshness]
 * turns the two stamps into the one freshness state the refresher acts on.
 */
@Entity(
    tableName = "playlists",
    indices = [Index(value = ["url"], unique = true)],
)
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val url: String,
    val epgUrl: String? = null,
    val lastUpdatedMs: Long = 0,
    val epgLastUpdatedMs: Long = 0,
    val epgFailedAtMs: Long = 0,
)
