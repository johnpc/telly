package com.johncorser.telly.features.catchup

import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Programme jumps during catch-up: the transport's ⏮/⏭ hop to the
 * neighbouring programme's archive on the same channel (per
 * [CatchupNeighbours]; ⏭ past the newest archive returns to live), and the
 * rewind-live entry into the airing programme's archive near the live edge.
 * A hop keeps the entry origin, so BACK still returns where catch-up was
 * entered from.
 */
class CatchupProgrammeHop(
    private val neighbours: CatchupNeighbours,
    private val liveEdge: CatchupLiveEdge,
    private val scope: CoroutineScope,
    private val host: Host,
) {
    /** The playback mode's surface a jump drives. */
    class Host(
        val active: () -> CatchupState?,
        val enter: (CatchupRequest, Boolean) -> Unit,
        val toLive: () -> Unit,
        val seek: (Long) -> Unit,
    )

    /** ⏮: the previous programme's archive; outside the horizon = stay put. */
    fun previous() {
        val current = host.active() ?: return
        scope.launch {
            neighbours.previous(current.request)?.let { host.enter(it, current.fromLive) }
        }
    }

    /** ⏭: the next programme's archive; at the newest end back to live. */
    fun next() {
        val current = host.active() ?: return
        scope.launch {
            when (val jump = neighbours.next(current.request)) {
                is CatchupJump.Archive -> host.enter(jump.request, current.fromLive)
                CatchupJump.Live -> host.toLive()
            }
        }
    }

    /** RW-rewinds-live: the airing programme's archive at live edge − skip. */
    fun rewindFromLive(
        channel: ChannelEntity?,
        deltaMs: Long,
        nowMs: Long,
    ) {
        if (channel == null) return
        scope.launch {
            val request = liveEdge.requestFor(channel) ?: return@launch
            host.enter(request, true)
            host.seek((nowMs - deltaMs - request.startMs).coerceAtLeast(0L))
        }
    }
}
