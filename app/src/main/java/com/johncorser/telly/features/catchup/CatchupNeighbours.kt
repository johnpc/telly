package com.johncorser.telly.features.catchup

import com.johncorser.telly.features.epg.EpgRepository
import com.johncorser.telly.features.epg.ProgramTitle
import com.johncorser.telly.features.epg.db.ProgramEntity
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.flow.first

/** Where a transport ⏮/⏭ hop lands. */
sealed interface CatchupJump {
    data class Archive(
        val request: CatchupRequest,
    ) : CatchupJump

    /** Past the newest archive (or off the EPG edge): back to live. */
    data object Live : CatchupJump
}

/**
 * Resolves the EPG neighbours of an archived programme for the transport's
 * ⏮/⏭ buttons: previous = the newest programme ending at or before the
 * archive's start, next = the oldest one starting at or after its end.
 * Only fully-aired programmes inside the catchup-days horizon build an
 * archive; anything newer (the airing programme, an EPG edge) is [CatchupJump.Live].
 */
class CatchupNeighbours(
    private val epg: EpgRepository,
    private val clock: () -> Long,
) {
    /** The playable previous programme's request, or null (= stay put). */
    suspend fun previous(request: CatchupRequest): CatchupRequest? =
        programmes(request, request.startMs - SCAN_MS, request.startMs)
            .filter { it.endMs <= request.startMs }
            .maxByOrNull { it.startMs }
            ?.let { archiveOf(request.channel, it) }

    /** The next programme's archive, or [CatchupJump.Live] at the newest end. */
    suspend fun next(request: CatchupRequest): CatchupJump {
        val neighbour =
            programmes(request, request.endMs, request.endMs + SCAN_MS)
                .filter { it.startMs >= request.endMs }
                .minByOrNull { it.startMs } ?: return CatchupJump.Live
        if (neighbour.endMs > clock()) return CatchupJump.Live
        return archiveOf(request.channel, neighbour)?.let { CatchupJump.Archive(it) } ?: CatchupJump.Live
    }

    private suspend fun programmes(
        request: CatchupRequest,
        fromMs: Long,
        toMs: Long,
    ): List<ProgramEntity> {
        val tvgId = request.channel.epgId ?: return emptyList()
        return epg.programsFor(listOf(tvgId), fromMs, toMs).first()
    }

    /** Builds the neighbour's request; null outside the catchup-days horizon. */
    private fun archiveOf(
        channel: ChannelEntity,
        programme: ProgramEntity,
    ): CatchupRequest? {
        val now = clock()
        val attributes =
            channel.catchupAttributes()?.takeIf {
                CatchupPlayability.playable(channel, programme.startMs, programme.endMs, hasInfo = true, nowMs = now)
            } ?: return null
        return CatchupUrlBuilder
            .build(channel.source.streamUrl, attributes, programme.startMs, programme.endMs, now)
            ?.let { url ->
                CatchupRequest(channel, url, ProgramTitle.of(programme.details), programme.startMs, programme.endMs)
            }
    }

    private companion object {
        /** Neighbour lookup window; programmes are at most 90 min in practice. */
        const val SCAN_MS = 6 * 3_600_000L
    }
}
