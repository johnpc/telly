package com.johncorser.telly.features.recording

import com.johncorser.telly.features.recording.db.RecordingEntity
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
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
        val stopSignal = CompletableDeferred<Unit>()
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
        capture.stopSignal.complete(Unit)
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
            // A row cancelled or deleted between the scheduler's sweep and this
            // launch is no longer SCHEDULED: nothing to record, nothing to finish.
            if (!store.markStarted(entry.id)) return
            var idleAttempts = 0
            while (keepGoing(capture, idleAttempts)) {
                idleAttempts = if (attempt(capture) > 0) 0 else idleAttempts + 1
                if (keepGoing(capture, idleAttempts)) awaitRetry(capture)
            }
            finish(entry)
        } finally {
            active.remove(entry.id)
            syncService()
        }
    }

    /** The loop runs until a user stop, the planned end, or three idle attempts in a row. */
    private fun keepGoing(
        capture: Active,
        idleAttempts: Int,
    ): Boolean = !capture.stopped && clock() < capture.entry.plannedEndMs && idleAttempts < MAX_IDLE_ATTEMPTS

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

    /**
     * The between-attempts pause, cut short the moment a user stop lands —
     * a plain delay here kept the row RECORDING for up to a second after
     * Stop was confirmed, so an immediate OK on the row re-opened the stop
     * confirm instead of playing the capture.
     */
    private suspend fun awaitRetry(capture: Active) {
        withTimeoutOrNull(RETRY_DELAY_MS) { capture.stopSignal.await() }
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
