package com.johncorser.telly.features.catchup

import com.johncorser.telly.features.epg.ProgramTitle
import com.johncorser.telly.features.guide.GuideCell
import com.johncorser.telly.features.playlist.db.ChannelEntity

/**
 * The guide's half of catch-up (ux-spec §3.17): OK on a playable past cell
 * resolves it to a catch-up URL, parks the request in the shared session
 * and pushes fullscreen playback, which consumes it.
 */
class GuideCatchup(
    private val session: CatchupSession,
    private val clock: () -> Long,
    private val goFullscreen: () -> Unit,
) {
    fun play(
        channel: ChannelEntity,
        cell: GuideCell,
    ) {
        val attributes = channel.catchupAttributes() ?: return
        val url =
            CatchupUrlBuilder.build(channel.source.streamUrl, attributes, cell.startMs, cell.endMs, clock())
                ?: return
        val title = cell.program?.details?.let(ProgramTitle::of)
        session.set(CatchupRequest(channel, url, title, cell.startMs, cell.endMs))
        goFullscreen()
    }
}
