package com.johncorser.telly.features.vod

import com.johncorser.telly.features.player.Media3PlayerEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The transport's player-facing half: pause/seek/clock sampling over the
 * engine's MockK-able ExoPlayer. Inert until [begin] — the resume prompt's
 * keys must never reach the player.
 */
class VodPlayerControls(
    private val engine: Media3PlayerEngine,
    val transport: VodTransportVisibility,
    private val persistOnPause: () -> Unit,
) {
    private val player get() = engine.player
    private var active = false

    private val mutablePaused = MutableStateFlow(false)
    val paused: StateFlow<Boolean> = mutablePaused.asStateFlow()

    private val mutableProgress = MutableStateFlow(VodProgress(0, 0))
    val progress: StateFlow<VodProgress> = mutableProgress.asStateFlow()

    /** Arms the controls once playback starts and reveals the transport. */
    fun begin() {
        active = true
        mutablePaused.value = false
        transport.poke(paused = false)
    }

    fun togglePause() {
        if (!active) return
        val nowPaused = !mutablePaused.value
        mutablePaused.value = nowPaused
        if (nowPaused) {
            player.pause()
            persistOnPause()
        } else {
            player.play()
        }
        transport.poke(nowPaused)
    }

    fun seekBy(deltaMs: Long) {
        if (!active) return
        val duration = durationMs()
        val target = (player.currentPosition + deltaMs).coerceAtLeast(0)
        player.seekTo(if (duration > 0) target.coerceAtMost(duration) else target)
        sample()
        transport.poke(mutablePaused.value)
    }

    fun sample() {
        mutableProgress.value = VodProgress(positionMs(), durationMs())
    }

    fun positionMs(): Long = player.currentPosition.coerceAtLeast(0)

    fun durationMs(): Long = player.duration.coerceAtLeast(0)
}
