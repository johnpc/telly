package com.johncorser.telly.core.refresh

import com.johncorser.telly.core.state.StateMachine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Runs the app's heavy background refreshes — EPG documents and playlist
 * imports — one at a time, each bounded by [timeoutMs], and logs every run.
 *
 * Both used to run concurrently from independent loops, each holding Room
 * write transactions over thousands of rows. On the Shield a launch could
 * leave both loops parked forever with no thread doing work and no error
 * logged (the guide stayed empty from Oct 21 to Nov 16, 2026). Serializing
 * them removes the contention; the timeout guarantees a wedged run is
 * abandoned so the next tick retries; the log line per run makes any
 * future stall visible in logcat instead of silent.
 */
@StateMachine("Background refresh gate")
class RefreshGate(
    private val clock: () -> Long = { 0L },
    private val log: (String) -> Unit = {},
    private val timeoutMs: Long = TIMEOUT_MS,
    /**
     * Where the timeout is measured. Production passes Dispatchers.Default so
     * the bound is always wall time, whatever dispatcher the caller is on
     * (under a test's virtual clock it would otherwise fire instantly while
     * real network IO runs); unit tests keep the caller's virtual time.
     */
    private val timerContext: CoroutineContext = EmptyCoroutineContext,
) {
    private val mutex = Mutex()

    /** [block]'s result, or [onTimeout] when it ran past [timeoutMs]. */
    suspend fun <T : Any> run(
        name: String,
        onTimeout: T,
        block: suspend () -> T,
    ): T =
        mutex.withLock {
            val startedAt = clock()
            val result = withContext(timerContext) { withTimeoutOrNull(timeoutMs) { block() } }
            val tookMs = clock() - startedAt
            // The per-minute "nothing due" checks take a few ms; only runs that
            // did real work (or were abandoned) are worth a log line.
            when {
                result == null -> log("$name: TIMED OUT after ${tookMs}ms, abandoned")
                tookMs >= QUIET_UNDER_MS -> log("$name: done in ${tookMs}ms")
            }
            result ?: onTimeout
        }

    companion object {
        /** Far above a full EPG import on the Shield (~30 s for 20k programmes). */
        const val TIMEOUT_MS: Long = 10L * 60L * 1000L

        /** Runs faster than this did no network work (nothing was due) and stay unlogged. */
        const val QUIET_UNDER_MS: Long = 500L
    }
}
