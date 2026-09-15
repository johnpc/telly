package com.johncorser.telly.features.playback

/**
 * Remembers a gate-intercepted archive tune so the verified PIN's re-tune
 * replays the catch-up URL instead of falling back to the live stream; any
 * other tune clears it.
 */
internal class PendingCatchup {
    private var pending: Pair<Long, String>? = null

    /** Called when the block gate intercepts; a live tune stashes nothing. */
    fun stash(
        channelId: Long,
        catchupUrl: String?,
    ) {
        pending = catchupUrl?.let { channelId to it }
    }

    /** The archive URL this tune should load (explicit beats replayed), or null for live. */
    fun consume(
        channelId: Long,
        catchupUrl: String?,
    ): String? {
        val url = catchupUrl ?: pending?.takeIf { it.first == channelId }?.second
        pending = null
        return url
    }
}
