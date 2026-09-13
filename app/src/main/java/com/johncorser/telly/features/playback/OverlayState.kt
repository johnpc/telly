package com.johncorser.telly.features.playback

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Holds the active overlay and the info-overlay auto-hide timer. The timer
 * runs on the injected scope's dispatcher, so tests drive it with virtual
 * time — no wall clock anywhere.
 */
class OverlayState(
    private val scope: CoroutineScope,
    private val timeoutMs: Long,
) {
    private val mutable = MutableStateFlow<PlaybackOverlay>(PlaybackOverlay.None)
    private var hideJob: Job? = null

    val overlay: StateFlow<PlaybackOverlay> = mutable.asStateFlow()
    val value: PlaybackOverlay get() = mutable.value

    fun set(next: PlaybackOverlay) {
        if (next != PlaybackOverlay.Info) hideJob?.cancel()
        mutable.value = next
    }

    /** Shows the info overlay and schedules its auto-hide. */
    fun showInfoAutoHiding() {
        set(PlaybackOverlay.Info)
        keepInfoAlive()
    }

    /** Restarts the auto-hide countdown (D-pad browsing keeps the overlay up). */
    fun keepInfoAlive() {
        hideJob?.cancel()
        hideJob =
            scope.launch {
                delay(timeoutMs)
                mutable.compareAndSet(PlaybackOverlay.Info, PlaybackOverlay.None)
            }
    }
}
