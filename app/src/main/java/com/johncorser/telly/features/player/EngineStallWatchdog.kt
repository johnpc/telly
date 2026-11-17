package com.johncorser.telly.features.player

import android.os.Handler
import androidx.media3.exoplayer.ExoPlayer

/**
 * The engine's deferred-work seams: [schedule] posts reconnect retries and
 * [stallTicker] the watchdog's periodic samples. Prod builds both on the
 * player's own thread ([forPlayer]); tests inject capturing lists, with a
 * null [stallTicker] disabling the watchdog so they opt in explicitly.
 */
class EngineSchedulers(
    val schedule: (Long, () -> Unit) -> Unit,
    val stallTicker: ((Long, () -> Unit) -> Unit)? = null,
    /** The engine's observation seam: playback health log (prod builds wire one). */
    val telemetry: PlaybackTelemetry? = null,
) {
    /** The engine's watchdog when the ticker seam is wired, else null; each sample also feeds [telemetry]. */
    fun watchdog(
        player: ExoPlayer,
        stateOf: () -> PlayerState,
        onStall: () -> Unit,
    ): EngineStallWatchdog? =
        stallTicker?.let {
            EngineStallWatchdog(player, StallDetector(), it, stateOf, onStall) {
                telemetry?.onSample(player.isPlaying, player.currentPosition, player.totalBufferedDuration)
            }
        }

    companion object {
        fun forPlayer(
            player: ExoPlayer,
            telemetry: PlaybackTelemetry? = null,
        ): EngineSchedulers {
            val post: (Long, () -> Unit) -> Unit = { d, t -> Handler(player.applicationLooper).postDelayed(t, d) }
            return EngineSchedulers(schedule = post, stallTicker = post, telemetry = telemetry)
        }
    }
}

/**
 * Samples playback every [tickMs] through the engine's scheduler and asks
 * [StallDetector] whether the stream is silently wedged (frozen frame or
 * never-ending buffering with no player error). [onStall] then recovers
 * exactly like a dropped stream — the fix that used to require backing out
 * of the player and tuning again by hand.
 */
class EngineStallWatchdog(
    private val player: ExoPlayer,
    private val detector: StallDetector,
    private val schedule: (Long, () -> Unit) -> Unit,
    private val stateOf: () -> PlayerState,
    private val onStall: () -> Unit,
    /** Each sample also feeds the playback telemetry (frozen-video check). */
    private val onSample: () -> Unit = {},
) {
    private var stopped = false

    fun start() = tick()

    /** Engine release: the periodic sampling must not outlive the player. */
    fun stop() {
        stopped = true
    }

    private fun tick() {
        schedule(TICK_MS) {
            if (stopped) return@schedule
            onSample()
            if (detector.onSample(stateOf(), player.playWhenReady, player.currentPosition)) onStall()
            tick()
        }
    }

    companion object {
        /** 2 s sampling: frozen video recovers in ~10 s, wedged buffering in ~30 s. */
        const val TICK_MS = 2_000L
    }
}
