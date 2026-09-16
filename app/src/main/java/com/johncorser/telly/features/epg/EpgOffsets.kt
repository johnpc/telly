package com.johncorser.telly.features.epg

import com.johncorser.telly.features.epg.db.ProgramEntity
import com.johncorser.telly.features.playlist.db.TvgOffset

/**
 * Per-channel "EPG time offset" math (Channel options §41): a channel's
 * programme times are SHIFTED by its offset for every lookup, so the guide
 * grid, info overlays and now/next all move together. Because the stored
 * rows keep their real times, a window query must reach `offset` further
 * back (or forward) to catch rows that shift INTO the window; the padded
 * superset is shifted and trimmed here.
 */
object EpgOffsets {
    /** tvgId -> offset millis; first channel wins a shared tvg-id. */
    fun ofMinutes(offsets: List<TvgOffset>): Map<String, Long> {
        val out = LinkedHashMap<String, Long>()
        offsets.forEach { row ->
            val tvgId = row.tvgId ?: return@forEach
            out.putIfAbsent(tvgId, row.epgOffsetMinutes * MINUTE_MS)
        }
        return out
    }

    /** Query lower bound catching rows that shift forward into the window. */
    fun queryFrom(
        fromMs: Long,
        offsets: Map<String, Long>,
    ): Long = fromMs - (offsets.values.maxOrNull()?.coerceAtLeast(0L) ?: 0L)

    /** Query upper bound catching rows that shift backward into the window. */
    fun queryTo(
        toMs: Long,
        offsets: Map<String, Long>,
    ): Long = toMs - (offsets.values.minOrNull()?.coerceAtMost(0L) ?: 0L)

    /** Each programme moved by its channel's offset (0 when none). */
    fun shifted(
        programs: List<ProgramEntity>,
        offsets: Map<String, Long>,
    ): List<ProgramEntity> {
        if (offsets.isEmpty()) return programs
        return programs.map { program ->
            val offset = offsets[program.channelTvgId] ?: 0L
            if (offset == 0L) {
                program
            } else {
                program.copy(
                    startMs = program.startMs + offset,
                    endMs = program.endMs + offset,
                )
            }
        }
    }

    /** Shift, then trim back to the requested [fromMs, toMs) display window. */
    fun windowed(
        programs: List<ProgramEntity>,
        offsets: Map<String, Long>,
        fromMs: Long,
        toMs: Long,
    ): List<ProgramEntity> = shifted(programs, offsets).filter { it.endMs > fromMs && it.startMs < toMs }

    private const val MINUTE_MS = 60_000L
}
