package com.johncorser.telly.features.vod

import com.johncorser.telly.features.vod.db.VodPositionDao
import com.johncorser.telly.features.vod.db.VodPositionEntity

/**
 * Resume-position policy over the vod_positions table: nothing is read or
 * written while "Remember playback position" is off, finished items clear
 * their row, and unknown durations are never persisted.
 */
class VodPositionStore(
    private val dao: VodPositionDao,
    private val remember: () -> Boolean,
    private val clock: () -> Long,
) {
    suspend fun read(itemKey: String): VodPositionEntity? = if (remember()) dao.byKey(itemKey) else null

    suspend fun save(
        itemKey: String,
        positionMs: Long,
        durationMs: Long,
    ) {
        if (!remember() || durationMs <= 0) return
        if (VodResumePolicy.finished(positionMs, durationMs)) {
            dao.delete(itemKey)
        } else {
            dao.upsert(
                VodPositionEntity(
                    itemKey = itemKey,
                    positionMs = positionMs,
                    durationMs = durationMs,
                    updatedAtMs = clock(),
                ),
            )
        }
    }
}
