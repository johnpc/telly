package com.johncorser.telly.features.catchup

import com.johncorser.telly.features.playlist.db.ChannelEntity

/**
 * Whether a PAST guide cell is playable as catch-up: the channel declares a
 * usable catch-up capability, the cell holds a real programme (not an EPG
 * "No information" filler), it has fully aired, and its start still lies
 * within the channel's `catchup-days` horizon. Airing/future cells and
 * anything else keep today's behavior.
 */
object CatchupPlayability {
    private const val DAY_MS = 24 * 3_600_000L

    fun playable(
        channel: ChannelEntity,
        startMs: Long,
        endMs: Long,
        hasInfo: Boolean,
        nowMs: Long,
    ): Boolean {
        if (!hasInfo || endMs > nowMs) return false
        val attributes = channel.catchupAttributes() ?: return false
        return startMs >= nowMs - attributes.days * DAY_MS
    }
}
