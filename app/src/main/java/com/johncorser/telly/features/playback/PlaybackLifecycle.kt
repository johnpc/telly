package com.johncorser.telly.features.playback

/**
 * Foreground/background transitions for a screen that owns the tuner.
 * Activity STOP stops the stream — the reference abandons audio focus and
 * releases its codecs the moment it is backgrounded — and the START after a
 * stop first runs [onForegrounded] (re-seeding sampled time state), then
 * [recover] to bring playback back: the guide re-tunes its preview, the
 * fullscreen route leaves for the guide like the reference, which lands on
 * the TV guide on every resume (round7 resume verification, 90 s and 11 min
 * probes). Without recovery a live stream that went stale while the process
 * sat frozen in the background never restarts and the surface stays black.
 *
 * [shouldStop] makes the stop PIP-aware: while the activity sits in the
 * picture-in-picture window the video must keep playing, so its host passes
 * a predicate that vetoes the stop for that state (PipState, features/pip).
 */
class PlaybackLifecycle(
    private val tuner: TuneController,
    private val onForegrounded: () -> Unit = {},
    private val recover: () -> Unit = {},
    private val shouldStop: () -> Boolean = { true },
) {
    fun onBackground() {
        if (shouldStop()) tuner.suspendPlayback()
    }

    fun onForeground() {
        onForegrounded()
        if (tuner.consumeSuspension()) recover()
    }
}
