package com.johncorser.telly.features.playback

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Holds the active overlay and the auto-hide timer of the transient ones
 * (info / transport / zap / quick-bar), each with its own timeout. The timer
 * runs on the injected scope's dispatcher, so tests drive it with virtual
 * time — no wall clock anywhere.
 */
class OverlayState(
    private val scope: CoroutineScope,
) {
    private val mutable = MutableStateFlow<PlaybackOverlay>(PlaybackOverlay.None)
    private var hideJob: Job? = null
    private var activeTimeoutMs = 0L

    val overlay: StateFlow<PlaybackOverlay> = mutable.asStateFlow()
    val value: PlaybackOverlay get() = mutable.value

    /** Shows a sticky overlay (panel, menus); cancels any auto-hide timer. */
    fun set(next: PlaybackOverlay) {
        hideJob?.cancel()
        mutable.value = next
    }

    /** Shows [next] and schedules its auto-hide after [timeoutMs]. */
    fun showAutoHiding(
        next: PlaybackOverlay,
        timeoutMs: Long,
    ) {
        mutable.value = next
        activeTimeoutMs = timeoutMs
        restart()
    }

    /** Restarts the countdown (D-pad browsing keeps a transient overlay up). */
    fun keepAlive() {
        if (hideJob?.isActive == true) restart()
    }

    private fun restart() {
        hideJob?.cancel()
        val shown = mutable.value
        hideJob =
            scope.launch {
                delay(activeTimeoutMs)
                mutable.compareAndSet(shown, PlaybackOverlay.None)
            }
    }
}
