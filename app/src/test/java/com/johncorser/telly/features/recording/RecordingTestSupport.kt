package com.johncorser.telly.features.recording

import com.johncorser.telly.features.recording.db.RecordingDao
import com.johncorser.telly.features.recording.db.RecordingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.io.File
import java.nio.file.Files

/** In-memory [RecordingDao] mirroring the real one's ordering. */
class FakeRecordingDao : RecordingDao {
    val rows = MutableStateFlow<List<RecordingEntity>>(emptyList())
    private var nextId = 1L

    override suspend fun insert(recording: RecordingEntity): Long {
        val id = nextId++
        rows.update { it + recording.copy(id = id) }
        return id
    }

    override fun observeAll(): Flow<List<RecordingEntity>> =
        rows.map { list ->
            list.sortedWith(compareByDescending<RecordingEntity> { it.startMs }.thenByDescending { it.id })
        }

    override fun observeScheduled(): Flow<List<RecordingEntity>> =
        rows.map { list ->
            list
                .filter { it.status == RecordingStatus.SCHEDULED.name }
                .sortedWith(compareBy({ it.startMs }, { it.id }))
        }

    override suspend fun byId(id: Long): RecordingEntity? = rows.value.firstOrNull { it.id == id }

    override suspend fun byStatus(status: String): List<RecordingEntity> =
        rows.value.filter { it.status == status }.sortedBy { it.id }

    override suspend fun countPlanned(
        channelKey: String,
        startMs: Long,
    ): Int =
        rows.value.count {
            it.channelKey == channelKey &&
                it.startMs == startMs &&
                it.status in listOf(RecordingStatus.SCHEDULED.name, RecordingStatus.RECORDING.name)
        }

    override suspend fun finish(
        id: Long,
        status: String,
        endMs: Long,
        sizeBytes: Long,
    ) {
        rows.update { list ->
            list.map { if (it.id == id) it.copy(status = status, endMs = endMs, sizeBytes = sizeBytes) else it }
        }
    }

    override suspend fun markStarted(
        id: Long,
        status: String,
        startMs: Long,
    ) {
        rows.update { list -> list.map { if (it.id == id) it.copy(status = status, startMs = startMs) else it } }
    }

    override suspend fun delete(id: Long) {
        rows.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun deleteAll() {
        rows.value = emptyList()
    }
}

/**
 * A recording row with sensible defaults for logic tests. Rarely-varied
 * fields (channel name/stream/title, end, size) stay fixed — tests that
 * need them use `.copy(...)` — so the factory stays within the parameter
 * budget.
 */
fun recordingEntity(
    channelKey: String = "tvg-1",
    filePath: String = "",
    startMs: Long = 1_000_000L,
    plannedEndMs: Long = 2_000_000L,
    status: RecordingStatus = RecordingStatus.SCHEDULED,
): RecordingEntity =
    RecordingEntity(
        id = 0,
        channelKey = channelKey,
        channelName = "News One",
        streamUrl = "http://s/1.ts",
        title = "Morning Report",
        filePath = filePath,
        startMs = startMs,
        plannedEndMs = plannedEndMs,
        endMs = null,
        status = status.name,
        sizeBytes = 0,
    )

/** A throwaway capture dir + [RecordingFiles] over it. */
fun tempRecordingFiles(): Pair<File, RecordingFiles> {
    val dir = Files.createTempDirectory("recordings-test").toFile()
    return dir to RecordingFiles(directory = { dir }, zone = java.util.TimeZone.getTimeZone("UTC"))
}

/** Collected [RecordingServiceControl] calls. */
class FakeServiceControl : RecordingServiceControl {
    val synced = mutableListOf<List<RecordingSession>>()

    override fun sync(sessions: List<RecordingSession>) {
        synced += sessions
    }
}

/**
 * A recorder that writes one chunk then goes idle, so a capture winds down
 * to DONE after the engine's idle attempts (keeps advanceUntilIdle finite).
 */
fun oneShotRecorder(): StreamRecorder {
    var wrote = false
    return StreamRecorder { _, sink, _ ->
        if (!wrote) {
            wrote = true
            sink.writeText("x")
            1L
        } else {
            0L
        }
    }
}

/** A recorder that always writes a chunk but reports zero (idle) progress. */
fun idleRecorder(): StreamRecorder =
    StreamRecorder { _, sink, _ ->
        sink.writeText("x")
        0L
    }
