package com.johncorser.telly.features.recording

import com.johncorser.telly.features.recording.db.RecordingEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Runs the actual captures: one copy loop per active recording on the
 * injected scope, re-connecting when a live source drops, finishing the
 * row as DONE (planned end reached or user stop) or FAILED (three
 * attempts in a row wrote nothing). The foreground service is kept in
 * sync with the active set; the injected clock is the only time source.
 */
class RecordingEngine(
    private val store: RecordingStore,
    private val recorder: StreamRecorder,
    private val files: RecordingFiles,
    private val scope: CoroutineScope,
    private val clock: () -> Long,
    private val service: RecordingServiceControl = RecordingServiceControl {},
) {
    private class Active(
        val entry: RecordingEntity,
        val startedAtMs: Long,
    ) {
        @Volatile var stopped: Boolean = false
        lateinit var job: Job
    }

    private val active = ConcurrentHashMap<Long, Active>()

    fun isActive(id: Long): Boolean = active.containsKey(id)

    /** Begins capturing [entry] (idempotent per row id). */
    fun start(entry: RecordingEntity) {
        if (active.containsKey(entry.id)) return
        val capture = Active(entry, clock())
        active[entry.id] = capture
        capture.job = scope.launch(start = CoroutineStart.LAZY) { run(capture) }
        syncService()
        capture.job.start()
    }

    /** User stop: ends the copy loop and waits for the row to finalize. */
    suspend fun stop(id: Long) {
        val capture = active[id] ?: return
        capture.stopped = true
        capture.job.join()
    }

    /** App start: rows stuck RECORDING from a killed process finalize now. */
    suspend fun recoverStale() {
        store.allRecording().forEach { entry ->
            if (!active.containsKey(entry.id)) finish(entry)
        }
    }

    private suspend fun run(capture: Active) {
        val entry = capture.entry
        try {
            store.markStarted(entry.id)
            var idleAttempts = 0
            while (!capture.stopped && clock() < entry.plannedEndMs && idleAttempts < MAX_IDLE_ATTEMPTS) {
                val written = attempt(capture)
                idleAttempts = if (written > 0) 0 else idleAttempts + 1
                if (!capture.stopped && clock() < entry.plannedEndMs && idleAttempts < MAX_IDLE_ATTEMPTS) {
                    delay(RETRY_DELAY_MS)
                }
            }
            finish(entry)
        } finally {
            active.remove(entry.id)
            syncService()
        }
    }

    /** One copy attempt; connection errors count as an idle attempt. */
    private suspend fun attempt(capture: Active): Long {
        val entry = capture.entry
        val stopNow = { capture.stopped || clock() >= entry.plannedEndMs }
        return try {
            recorder.copy(entry.streamUrl, File(entry.filePath), stopNow)
        } catch (_: java.io.IOException) {
            0L
        }
    }

    /** DONE when bytes landed on disk, FAILED otherwise. */
    private suspend fun finish(entry: RecordingEntity) {
        val size = files.sizeOf(entry.filePath)
        if (size > 0) store.complete(entry.id, size) else store.fail(entry.id, size)
    }

    private fun syncService() {
        service.sync(active.values.map { RecordingSession(it.entry.channelName, it.startedAtMs) })
    }

    companion object {
        private const val MAX_IDLE_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 1_000L
    }
}
