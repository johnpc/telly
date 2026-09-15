package com.johncorser.telly.features.epg.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One user-added (custom) EPG source. The reference assigns EPG sources per
 * playlist (capture 58's footer: "EPG sources should be assigned in the
 * playlist settings"), so rows key on the playlist's stable identity — its
 * URL — which survives playlist re-imports. The auto-detected source stays
 * on the playlist row itself (`playlists.epgUrl`, from `url-tvg`).
 * [addedAtMs] (injected clock) fixes the fetch/merge order.
 */
@Entity(
    tableName = "epg_sources",
    indices = [Index(value = ["playlistUrl", "url"], unique = true)],
)
data class EpgSourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistUrl: String,
    val url: String,
    val addedAtMs: Long,
)
