package com.johncorser.telly.features.recording

import com.johncorser.telly.features.recording.db.RecordingDao
import com.johncorser.telly.features.recording.db.RecordingEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room-backed state of the DVR: inserts entries, walks their status
 * transitions with the injected clock, and feeds the library list. All
 * file-system work stays in [RecordingFiles]; this class only owns rows.
 */
class RecordingStore(
    private val dao: RecordingDao,
    private val clock: () -> Long,
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

    /** SCHEDULED -> RECORDING; the start stamp becomes the actual start. */
    suspend fun markStarted(id: Long) = dao.markStarted(id, RecordingStatus.RECORDING.name, clock())

    /** RECORDING -> DONE with the final size; also used by the user's Stop. */
    suspend fun complete(
        id: Long,
        sizeBytes: Long,
    ) = dao.finish(id, RecordingStatus.DONE.name, clock(), sizeBytes)

    /** Any state -> FAILED (copy gave up, or the start window was missed). */
    suspend fun fail(
        id: Long,
        sizeBytes: Long = 0,
    ) = dao.finish(id, RecordingStatus.FAILED.name, clock(), sizeBytes)

    suspend fun delete(id: Long) = dao.delete(id)

    suspend fun deleteAll() = dao.deleteAll()
}
