package com.johncorser.telly.features.catchup

import com.johncorser.telly.features.playlist.db.ChannelEntity

/** One playable already-aired programme, resolved to its catch-up URL. */
data class CatchupRequest(
    val channel: ChannelEntity,
    val url: String,
    val title: String?,
    val startMs: Long,
    val endMs: Long,
) {
    val durationMs: Long get() = endMs - startMs
}

/**
 * One-shot hand-off slot between the guide (which resolves a past cell to a
 * catch-up request and pushes the playback route) and the playback screen
 * (which consumes the request instead of tuning the live stream).
 */
class CatchupSession {
    private var pending: CatchupRequest? = null

    fun set(request: CatchupRequest) {
        pending = request
    }

    fun consume(): CatchupRequest? = pending.also { pending = null }
}
