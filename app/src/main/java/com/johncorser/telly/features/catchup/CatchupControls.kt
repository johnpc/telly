package com.johncorser.telly.features.catchup

import com.johncorser.telly.features.playback.PlaybackKey
import com.johncorser.telly.features.playback.PlaybackOverlay
import com.johncorser.telly.features.player.PlayerEngine
import kotlinx.coroutines.flow.StateFlow

/**
 * The transport ⏸ during catch-up: pause pins the info/transport overlay
 * (the VOD stays-up-while-paused idiom) and resume re-arms its auto-hide.
 * Inert outside catch-up mode — live pause is timeshift, a later slice.
 */
class CatchupPause(
    private val engine: PlayerEngine,
    private val active: () -> Boolean,
    private val show: () -> Unit,
    private val pin: () -> Unit,
) {
    val paused: StateFlow<Boolean> = engine.paused

    fun toggle() {
        if (!active()) return
        if (engine.paused.value) {
            engine.resume()
            show()
        } else {
            engine.pause()
            pin()
        }
    }
}

/**
 * Routes a key through [CatchupKeyPolicy] and executes the resulting
 * command against the playback mode; true = the key was a catch-up action.
 */
class CatchupKeyRouting(
    private val deps: CatchupDeps,
    private val host: CatchupPlayback,
) {
    fun onKey(
        overlay: PlaybackOverlay,
        key: PlaybackKey,
    ): Boolean {
        val command =
            CatchupKeyPolicy.commandFor(overlay, key, host.mode(), deps.toggles.snapshot(), deps.skip())
                ?: return false
        when (command) {
            is CatchupCommand.Seek -> host.seekBy(command.deltaMs)
            is CatchupCommand.RewindLive -> host.rewindLive(command.deltaMs)
            CatchupCommand.Back -> host.back()
        }
        return true
    }
}
