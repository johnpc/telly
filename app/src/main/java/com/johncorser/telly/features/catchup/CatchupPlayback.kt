package com.johncorser.telly.features.catchup

import com.johncorser.telly.features.playback.PlaybackEnv
import com.johncorser.telly.features.playback.PlaybackKey
import com.johncorser.telly.features.playback.PlaybackOverlay
import com.johncorser.telly.features.playback.TuneController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** The catch-up programme the player is on, and how it was entered. */
data class CatchupState(
    val request: CatchupRequest,
    val fromLive: Boolean,
)

/**
 * Catch-up playback mode inside the fullscreen player: consumes the guide's
 * pending request, owns the seek keys (via [CatchupKeyPolicy], honoring the
 * Remote-control toggles), tracks the position for the transport, and
 * leaves the mode on BACK (to live when entered from live, else to the
 * guide) or on any live tune.
 */
class CatchupPlayback(
    private val env: PlaybackEnv,
    private val tuner: TuneController,
    private val showTransport: () -> Unit,
    private val scope: CoroutineScope,
    private val exitToGuide: () -> Unit,
) {
    private val mutableState = MutableStateFlow<CatchupState?>(null)
    private val tracker = CatchupPosition(env.engine)
    private val liveEdge = CatchupLiveEdge(env.epgRepository, env.time.clock)

    val state: StateFlow<CatchupState?> = mutableState.asStateFlow()
    val position: StateFlow<Long> = tracker.position

    /** Consumes the guide's pending request; true = catch-up owns the tune. */
    fun resumePending(): Boolean {
        val request = env.hooks.catchup.session.consume() ?: return false
        enter(request, fromLive = false)
        return true
    }

    /** True when the key was a catch-up action (seek / rewind-live / back). */
    fun onKey(
        overlay: PlaybackOverlay,
        key: PlaybackKey,
    ): Boolean {
        val keys = env.hooks.catchup.toggles.snapshot()
        val command = CatchupKeyPolicy.commandFor(overlay, key, mode(), keys) ?: return false
        execute(command)
        return true
    }

    fun seekBy(deltaMs: Long) {
        val active = mutableState.value ?: return
        seekTo((env.engine.positionMs() + deltaMs).coerceIn(0L, active.request.durationMs))
    }

    /** Any live tune (zap, panel row, recents, BACK-to-live) leaves catch-up mode. */
    fun onLiveTune() {
        mutableState.value = null
    }

    /** The overlay's per-second ticker samples the engine while visible. */
    fun refreshPosition() {
        if (mutableState.value != null) tracker.refresh()
    }

    private fun mode(): CatchupMode =
        when {
            mutableState.value != null -> CatchupMode.PLAYING
            tuner.current.value?.catchupAttributes() != null -> CatchupMode.LIVE_CAPABLE
            else -> CatchupMode.NONE
        }

    /** BACK at bare catch-up playback returns where catch-up was entered from. */
    private fun execute(command: CatchupCommand) {
        when (command) {
            is CatchupCommand.Seek -> seekBy(command.deltaMs)
            is CatchupCommand.RewindLive -> rewindFromLive(command.deltaMs)
            CatchupCommand.Back ->
                mutableState.value?.let { active ->
                    onLiveTune()
                    if (active.fromLive) tuner.tune(active.request.channel) else exitToGuide()
                }
        }
    }

    private fun enter(
        request: CatchupRequest,
        fromLive: Boolean,
    ) {
        mutableState.value = CatchupState(request, fromLive)
        tracker.reset()
        tuner.tune(request.channel, catchupUrl = request.url)
        showTransport()
    }

    private fun rewindFromLive(deltaMs: Long) {
        val channel = tuner.current.value ?: return
        scope.launch {
            val request = liveEdge.requestFor(channel) ?: return@launch
            enter(request, fromLive = true)
            seekTo((env.time.clock() - deltaMs - request.startMs).coerceAtLeast(0L))
        }
    }

    private fun seekTo(positionMs: Long) {
        env.engine.seekTo(positionMs)
        tracker.set(positionMs)
        showTransport()
    }
}
