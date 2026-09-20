package com.johncorser.telly.features.player

import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.ExoPlayer

/**
 * Auto-reconnect for dropped live streams: while the [policy]'s backoff
 * budget lasts, a re-`prepare()` is posted through [schedule] (the player's
 * own thread in prod, a capturing list in tests) and the engine surfaces
 * Reconnecting; a spent budget surfaces the hard error instead.
 */
class EngineReconnector(
    private val player: ExoPlayer,
    private val policy: ReconnectPolicy,
    private val schedule: (Long, () -> Unit) -> Unit,
) {
    fun reset() = policy.reset()

    /** The state to surface for [error]; schedules the retry when transient. */
    fun onError(error: PlaybackException): PlayerState {
        val delayMs = policy.nextDelayMs() ?: return PlayerState.Error(error.errorCodeName)
        // A live-window overrun rejoins the edge first; otherwise just re-prepare.
        schedule(delayMs) {
            if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) player.seekToDefaultPosition()
            player.prepare()
        }
        return PlayerState.Reconnecting
    }
}
