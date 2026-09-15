package com.johncorser.telly.features.catchup

import com.johncorser.telly.features.playback.PlaybackEnv
import com.johncorser.telly.features.playback.TuneController
import com.johncorser.telly.features.player.PlayerState
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
 * pending request, owns the seek keys (via [CatchupKeyRouting], honoring
 * the Remote-control toggles), the transport's [pause] and programme
 * [hop]s, tracks the position for the transport, and leaves the mode on
 * BACK (to live when entered from live, else to the guide), on any live
 * tune, or when the finished archive returns to live playback of the same
 * channel ([PlayerState.Ended]).
 */
class CatchupPlayback(
    private val env: PlaybackEnv,
    private val tuner: TuneController,
    private val showTransport: () -> Unit,
    pinTransport: () -> Unit,
    private val scope: CoroutineScope,
    private val exitToGuide: () -> Unit,
) {
    private val mutableState = MutableStateFlow<CatchupState?>(null)
    private val tracker = CatchupPosition(env.engine)

    /** The seek/rewind-live/back keys; true = the key was a catch-up action. */
    val keys = CatchupKeyRouting(env.hooks.catchup, this)

    val state: StateFlow<CatchupState?> = mutableState.asStateFlow()
    val position: StateFlow<Long> = tracker.position

    /** Transport ⏸: pause pins the overlay, resume re-arms its auto-hide. */
    val pause = CatchupPause(env.engine, { mutableState.value != null }, showTransport, pinTransport)

    /** Transport ⏮/⏭ + the rewind-live entry (programme jumps). */
    val hop =
        CatchupProgrammeHop(
            neighbours = CatchupNeighbours(env.epgRepository, env.time.clock),
            liveEdge = CatchupLiveEdge(env.epgRepository, env.time.clock),
            scope = scope,
            host = CatchupProgrammeHop.Host(mutableState::value, ::enter, ::toLive, ::seekTo),
        )

    init {
        // A finished archive returns to LIVE playback of the same channel.
        scope.launch { env.engine.state.collect { if (it == PlayerState.Ended) toLive() } }
    }

    /** Consumes the guide's pending request; true = catch-up owns the tune. */
    fun resumePending(): Boolean {
        val request = env.hooks.catchup.session.consume() ?: return false
        enter(request, fromLive = false)
        return true
    }

    /** The configured seek steps (Settings -> Playback -> "Skip steps"). */
    val skip: () -> CatchupSkip = env.hooks.catchup::skip

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

    internal fun mode(): CatchupMode =
        when {
            mutableState.value != null -> CatchupMode.PLAYING
            tuner.current.value?.catchupAttributes() != null -> CatchupMode.LIVE_CAPABLE
            else -> CatchupMode.NONE
        }

    internal fun rewindLive(deltaMs: Long) = hop.rewindFromLive(tuner.current.value, deltaMs, env.time.clock())

    /** BACK at bare catch-up playback returns where catch-up was entered from. */
    internal fun back() {
        val active = mutableState.value ?: return
        if (active.fromLive) return toLive()
        onLiveTune()
        exitToGuide()
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

    /** Archive end and the hop's newest edge both retune live in place. */
    private fun toLive() {
        val active = mutableState.value ?: return
        onLiveTune()
        tuner.tune(active.request.channel)
    }

    private fun seekTo(positionMs: Long) {
        env.engine.seekTo(positionMs)
        tracker.set(positionMs)
        showTransport()
    }
}
