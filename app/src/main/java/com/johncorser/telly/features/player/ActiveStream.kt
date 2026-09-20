package com.johncorser.telly.features.player

/**
 * Tracks the URL the engine is currently streaming so a re-load of the very
 * same stream is recognized and skipped: the guide preview and fullscreen
 * playback share one engine and each re-tunes the last channel on entry, so
 * honoring that load would only rebuffer the picture mid-hand-over.
 */
class ActiveStream {
    private var url: String? = null

    fun onLoad(streamUrl: String) {
        url = streamUrl
    }

    fun onStop() {
        url = null
    }

    /** True while [streamUrl] is already the live, unpaused pipeline. */
    fun isCurrent(
        streamUrl: String,
        state: PlayerState,
        paused: Boolean,
    ): Boolean =
        streamUrl == url &&
            !paused &&
            when (state) {
                PlayerState.Buffering, PlayerState.Playing, PlayerState.Reconnecting -> true
                else -> false
            }
}
