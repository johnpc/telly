package com.johncorser.telly.features.recording

import com.johncorser.telly.features.recording.db.RecordingDao
import com.johncorser.telly.features.recording.db.RecordingEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room-backed state of the DVR: inserts entries and feeds the library list;
 * the status transitions live in RecordingStoreTransitions.kt (the one
 * owner of the SCHEDULED -> RECORDING -> DONE | FAILED machine). All
 * file-system work stays in [RecordingFiles]; this class only owns rows.
 */
class RecordingStore(
    internal val dao: RecordingDao,
    internal val clock: () -> Long,
) {
    /** The library, newest start first. */
    val recordings: Flow<List<RecordingEntity>> = dao.observeAll()

    /** The scheduler's watch list, soonest start first. */
    val scheduled: Flow<List<RecordingEntity>> = dao.observeScheduled()

    /** Inserts a SCHEDULED entry; duplicates of a planned slot are ignored. */
    suspend fun schedule(entry: RecordingEntity): RecordingEntity? {
        if (dao.countPlanned(entry.channelKey, entry.startMs) > 0) return null
        val id = dao.insert(entry.copy(status = RecordingStatus.SCHEDULED.name))
        return entry.copy(id = id, status = RecordingStatus.SCHEDULED.name)
    }

    /** The in-progress recording of [channelKey], if any. */
    suspend fun activeFor(channelKey: String): RecordingEntity? =
        dao.byStatus(RecordingStatus.RECORDING.name).firstOrNull { it.channelKey == channelKey }

    suspend fun byId(id: Long): RecordingEntity? = dao.byId(id)

    /** Every entry currently claiming to record (recovery sweep input). */
    suspend fun allRecording(): List<RecordingEntity> = dao.byStatus(RecordingStatus.RECORDING.name)

    suspend fun delete(id: Long) = dao.delete(id)

    suspend fun deleteAll() = dao.deleteAll()
}
