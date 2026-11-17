package com.johncorser.telly.features.player

import com.johncorser.telly.core.state.StateMachine

/**
 * Playback health log, one line per notable event, so a reported freeze can
 * be matched to what the player actually did (`adb logcat -s telly-play`):
 * state changes with how long the previous state lasted (a Playing ->
 * Buffering -> Playing pair is a rebuffer and its duration), VIDEO FROZEN
 * when no frame has rendered for [FROZEN_VIDEO_MS] while the audio clock
 * keeps advancing (a decoder or timestamp stall that the position-based
 * StallDetector cannot see), dropped-frame bursts, audio underruns, load
 * errors and the decoders/format in use. Plain logic: clock and log
 * injected; the Media3 adapters feed it.
 */
@StateMachine("Playback health monitor")
class PlaybackTelemetry(
    private val clock: () -> Long,
    private val log: (String) -> Unit,
) {
    private var state = "IDLE"
    private var stateSinceMs = clock()
    private var lastFrameMs = 0L
    private var framesSinceLoad = false
    private var frozenSinceMs = NOT_FROZEN
    private var lastPositionMs = -1L

    fun onLoad(stream: String) {
        log("tune $stream")
        framesSinceLoad = false
        frozenSinceMs = NOT_FROZEN
    }

    /** [next] = the player's state label (IDLE / BUFFERING / READY / ENDED, playing or paused). */
    fun onState(
        next: String,
        positionMs: Long,
    ) {
        if (next == state) return
        log("state $state -> $next after ${clock() - stateSinceMs}ms at ${positionMs}ms")
        state = next
        stateSinceMs = clock()
    }

    /** Every rendered frame (the engine's frame-metadata listener). */
    fun onVideoFrame() {
        val now = clock()
        if (frozenSinceMs != NOT_FROZEN) log("video resumed after ${now - frozenSinceMs}ms frozen")
        frozenSinceMs = NOT_FROZEN
        lastFrameMs = now
        framesSinceLoad = true
    }

    /** The watchdog's periodic sample: audio clock advancing, no new frame. */
    fun onSample(
        playing: Boolean,
        positionMs: Long,
        bufferedMs: Long,
    ) {
        val audioAdvancing = playing && positionMs != lastPositionMs
        lastPositionMs = positionMs
        val gapMs = clock() - lastFrameMs
        if (audioAdvancing && newFreeze(gapMs)) {
            frozenSinceMs = lastFrameMs
            log("VIDEO FROZEN: no frame for ${gapMs}ms while audio advances, ${bufferedMs}ms buffered")
        }
    }

    /** Video had rendered since the tune, is not already reported, and stopped [FROZEN_VIDEO_MS] ago. */
    private fun newFreeze(gapMs: Long): Boolean =
        framesSinceLoad && frozenSinceMs == NOT_FROZEN && gapMs >= FROZEN_VIDEO_MS

    fun onDroppedFrames(
        count: Int,
        elapsedMs: Long,
    ) {
        if (count >= DROPPED_BURST) log("dropped $count frames in ${elapsedMs}ms")
    }

    fun onAudioUnderrun(bufferMs: Long) = log("audio underrun (${bufferMs}ms audio buffer)")

    fun onLoadError(cause: String) = log("load error: $cause")

    /** Decoder names, formats: the facts the TiviMate comparison needs. */
    fun note(message: String) = log(message)

    companion object {
        /** A second without a frame at broadcast rates (25–60 fps) is a visible freeze. */
        const val FROZEN_VIDEO_MS = 1_000L

        /** Fewer dropped frames than this per report are not visible. */
        const val DROPPED_BURST = 5

        private const val NOT_FROZEN = -1L
    }
}

/** A loggable stream label: host + last path segment, never credentials or query tokens. */
fun streamLabel(url: String): String {
    val noQuery = url.substringBefore('?')
    val afterScheme = noQuery.substringAfter("://")
    val host = afterScheme.substringBefore('/').substringAfterLast('@')
    val last = afterScheme.substringAfterLast('/', missingDelimiterValue = "")
    return if (last.isEmpty()) host else "$host/…/$last"
}
