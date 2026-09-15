package com.johncorser.telly.features.recording.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One DVR entry (recording-slice). The channel is referenced by the
 * playlist-refresh-stable identity key (row ids are reassigned on import)
 * plus a display-name/stream snapshot taken when the recording was created,
 * so entries stay meaningful even after the channel leaves the playlist.
 * `status` holds a [com.johncorser.telly.features.recording.RecordingStatus]
 * name; `endMs` is null until the recording actually finished or failed.
 */
@Entity(tableName = "recordings")
data class RecordingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val channelKey: String,
    val channelName: String,
    val streamUrl: String,
    val title: String,
    val filePath: String,
    val startMs: Long,
    val plannedEndMs: Long,
    val endMs: Long?,
    val status: String,
    val sizeBytes: Long,
)
