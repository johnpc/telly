package com.johncorser.telly.features.vod.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The stored resume position of one VOD item (ux-spec §VOD): persisted on
 * pause/stop/exit and every ~10 s of playback, cleared once the item is
 * watched past the finish threshold (VodResumePolicy).
 */
@Entity(tableName = "vod_positions")
data class VodPositionEntity(
    @PrimaryKey val itemKey: String,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAtMs: Long,
)
