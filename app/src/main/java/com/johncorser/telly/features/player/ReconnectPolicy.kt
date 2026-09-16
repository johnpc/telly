package com.johncorser.telly.features.player

/**
 * Live-stream auto-reconnect budget. On each player error the engine asks for
 * the [nextDelayMs] backoff — 1s, 2s, 4s, 8s… doubling and capped at
 * [maxDelayMs] — and re-prepares the current media item after it. Once
 * [maxAttempts] retries are spent [nextDelayMs] returns null and the engine
 * surfaces a hard error. A successful playback calls [reset], so a stream that
 * drops again hours later gets a fresh budget. Pure logic, no clock or Android
 * types — the engine owns the actual scheduling so this stays JVM-testable.
 */
class ReconnectPolicy(
    private val maxAttempts: Int = 6,
    private val baseDelayMs: Long = 1_000L,
    private val maxDelayMs: Long = 30_000L,
) {
    private var attempt = 0

    /** Next backoff delay in ms, or null once the retry budget is spent. */
    fun nextDelayMs(): Long? {
        if (attempt >= maxAttempts) return null
        val delay = (baseDelayMs shl attempt).coerceAtMost(maxDelayMs)
        attempt++
        return delay
    }

    /** Clears the spent attempts after a successful (re)connect. */
    fun reset() {
        attempt = 0
    }
}
