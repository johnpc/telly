package com.johncorser.telly.features.player

/**
 * Detects a silently wedged stream from periodic playback samples — the
 * failure auto-reconnect can't see because no onPlayerError ever fires:
 * the position freezing while the player still claims to be playing (dead
 * renderer pipeline / stalled source), or a buffering state that never
 * resolves. Pure logic: the watchdog owns the sampling cadence, so the
 * thresholds are consecutive tick counts, not wall time.
 */
class StallDetector(
    private val frozenTicks: Int = FROZEN_TICKS,
    private val bufferingTicks: Int = BUFFERING_TICKS,
) {
    private enum class Mode { NONE, FROZEN, BUFFERING }

    private var mode = Mode.NONE
    private var run = 0
    private var lastPositionMs = Long.MIN_VALUE

    /** Feeds one sample; true = wedged, recover now (the count restarts). */
    fun onSample(
        state: PlayerState,
        playWhenReady: Boolean,
        positionMs: Long,
    ): Boolean {
        val next = modeOf(state, playWhenReady, positionMs)
        run = if (next == mode) run + 1 else 1
        mode = next
        lastPositionMs = positionMs
        val limit =
            when (next) {
                Mode.FROZEN -> frozenTicks
                Mode.BUFFERING -> bufferingTicks
                Mode.NONE -> Int.MAX_VALUE
            }
        val wedged = run >= limit
        if (wedged) run = 0
        return wedged
    }

    /** A user pause is never a stall; reconnecting runs its own budget. */
    private fun modeOf(
        state: PlayerState,
        playWhenReady: Boolean,
        positionMs: Long,
    ): Mode =
        when {
            !playWhenReady -> Mode.NONE
            state == PlayerState.Buffering -> Mode.BUFFERING
            state == PlayerState.Playing && positionMs == lastPositionMs -> Mode.FROZEN
            else -> Mode.NONE
        }

    companion object {
        /** Consecutive 2 s samples with zero movement while "playing" (~10 s). */
        const val FROZEN_TICKS = 5

        /** Consecutive 2 s samples stuck buffering (~30 s — past the deepest
         * reconnect backoff, so a recovering stream is never double-kicked). */
        const val BUFFERING_TICKS = 15
    }
}
