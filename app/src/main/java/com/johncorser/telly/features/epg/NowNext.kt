package com.johncorser.telly.features.epg

import com.johncorser.telly.features.epg.db.ProgramEntity

/** The programme airing now and the one after it on a single channel. */
data class NowNext(
    val now: ProgramEntity? = null,
    val next: ProgramEntity? = null,
)

/** Pure fold from a window of programmes to per-channel now/next. */
object NowNextResolver {
    /**
     * [programs] must be ordered by (channelTvgId, startMs) and contain only
     * rows ending after [atMs] — exactly what ProgramDao.observeAiringOrUpcoming
     * emits. Channels without matching rows are absent from the result.
     */
    fun resolve(
        programs: List<ProgramEntity>,
        atMs: Long,
    ): Map<String, NowNext> =
        programs.groupBy { it.channelTvgId }.mapValues { (_, channelPrograms) ->
            NowNext(
                now = channelPrograms.firstOrNull { it.startMs <= atMs && it.endMs > atMs },
                next = channelPrograms.firstOrNull { it.startMs > atMs },
            )
        }
}
