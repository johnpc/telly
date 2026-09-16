package com.johncorser.telly.features.recording

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * One capture attempt's in-flight OkHttp call, so a user stop can cancel
 * it: the recorders' [shouldStop] checks only run BETWEEN blocking reads,
 * and a stalled (or slowly trickling) socket otherwise pins the copy loop —
 * and the row's RECORDING status — until the server lets go.
 */
class RecordingCalls {
    private val inFlight = AtomicReference<Call?>(null)

    @Volatile private var cancelled = false

    /** Registers the attempt's current call; already-stopped attempts cancel it at once. */
    fun track(call: Call): Call {
        inFlight.set(call)
        if (cancelled) call.cancel()
        return call
    }

    /** Cancels the in-flight call and every call tracked after (thread-safe, unblocks reads). */
    fun cancel() {
        cancelled = true
        inFlight.get()?.cancel()
    }
}

/**
 * Runs [block] under a watcher that cancels [calls] the moment
 * [shouldStop] turns true, so a stop lands within one check slice even
 * while the attempt is parked inside blocking call/read I/O.
 */
suspend fun <T> cancellingOnStop(
    calls: RecordingCalls,
    shouldStop: () -> Boolean,
    block: suspend () -> T,
): T =
    coroutineScope {
        val watcher =
            launch {
                while (!shouldStop()) delay(HLS_STOP_CHECK_SLICE_MS)
                calls.cancel()
            }
        try {
            block()
        } finally {
            watcher.cancel()
        }
    }

/**
 * The default between-polls wait, sliced so a user stop lands promptly —
 * one plain target-duration delay kept the engine's stop() joined for
 * whole seconds while the row still said RECORDING.
 */
internal suspend fun awaitNextHlsPoll(
    totalMs: Long,
    shouldStop: () -> Boolean,
) {
    var waitedMs = 0L
    while (waitedMs < totalMs && !shouldStop()) {
        delay(HLS_STOP_CHECK_SLICE_MS.coerceAtMost(totalMs - waitedMs))
        waitedMs += HLS_STOP_CHECK_SLICE_MS
    }
}

internal const val HLS_STOP_CHECK_SLICE_MS = 250L

/**
 * The capture-path HTTP client: the same redirect treatment the player
 * gives the identical URLs, plus bounded connect/read waits so a dead
 * source fails the attempt instead of hanging it.
 */
fun recordingHttpClient(): OkHttpClient =
    OkHttpClient
        .Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(RECORDING_CONNECT_TIMEOUT_S, TimeUnit.SECONDS)
        .readTimeout(RECORDING_READ_TIMEOUT_S, TimeUnit.SECONDS)
        .build()

internal const val RECORDING_CONNECT_TIMEOUT_S = 5L
internal const val RECORDING_READ_TIMEOUT_S = 10L
