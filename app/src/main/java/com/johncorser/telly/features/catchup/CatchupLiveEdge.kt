package com.johncorser.telly.features.catchup

import com.johncorser.telly.features.epg.EpgRepository
import com.johncorser.telly.features.epg.ProgramTitle
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.flow.first

/**
 * Resolves "rewind live into catch-up" (the RW/Left/Down-rewinds-live
 * toggles): the airing programme of the tuned catch-up channel, built into
 * a request whose stream starts at the programme start — the caller then
 * seeks to live edge minus the skip step.
 */
class CatchupLiveEdge(
    private val epg: EpgRepository,
    private val clock: () -> Long,
) {
    suspend fun requestFor(channel: ChannelEntity): CatchupRequest? {
        val attributes = channel.catchupAttributes() ?: return null
        val tvgId = channel.epgId ?: return null
        val now = clock()
        return epg.nowNext(listOf(tvgId), now).first()[tvgId]?.now?.let { airing ->
            CatchupUrlBuilder
                .build(channel.source.streamUrl, attributes, airing.startMs, airing.endMs, now)
                ?.let { url ->
                    CatchupRequest(channel, url, ProgramTitle.of(airing.details), airing.startMs, airing.endMs)
                }
        }
    }
}
