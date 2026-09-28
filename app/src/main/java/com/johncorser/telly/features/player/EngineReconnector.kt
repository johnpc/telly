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

    /**
     * A silently wedged stream (stall watchdog, no error event): the wedged
     * pipeline must be dropped before re-preparing, so the retry stops the
     * player, rejoins the live edge (finite archives keep their position)
     * and prepares again — on the same budget as [onError].
     */
    fun onStall(): PlayerState {
        val delayMs = policy.nextDelayMs() ?: return PlayerState.Error(STALLED)
        schedule(delayMs) {
            player.stop()
            if (player.isCurrentMediaItemLive) player.seekToDefaultPosition()
            player.prepare()
        }
        return PlayerState.Reconnecting
    }

    companion object {
        /** Displayable cause once a wedged stream spends the retry budget. */
        const val STALLED = "STREAM_STALLED"
    }
}
