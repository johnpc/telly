package com.johncorser.telly.features.guide

import com.johncorser.telly.features.epg.db.ProgramEntity

/**
 * Turns one channel's programmes into contiguous cells over a span. EPG
 * gaps (and fully EPG-less channels) become "No information" fillers like
 * TiviMate's, chunked at the span's 30-min grid so LEFT/RIGHT pan the
 * timeline in half-hour steps where no programme data exists.
 */
object GuideCellsBuilder {
    fun build(
        programs: List<ProgramEntity>,
        span: GuideSpan,
    ): List<GuideCell> {
        val cells = mutableListOf<GuideCell>()
        var cursorMs = span.fromMs
        for (program in programs.sortedBy { it.startMs }) {
            cursorMs = appendProgram(cells, program, cursorMs, span)
        }
        appendFillers(cells, cursorMs, span.toMs, span)
        return cells
    }

    /** Adds one programme (gap-filled) and returns the advanced cursor. */
    private fun appendProgram(
        cells: MutableList<GuideCell>,
        program: ProgramEntity,
        cursorMs: Long,
        span: GuideSpan,
    ): Long {
        val swallowed = program.endMs <= cursorMs
        if (swallowed || program.startMs >= span.toMs) return cursorMs
        appendFillers(cells, cursorMs, program.startMs, span)
        cells += GuideCell(program.startMs, program.endMs, program)
        return program.endMs
    }

    private fun appendFillers(
        cells: MutableList<GuideCell>,
        fromMs: Long,
        toMs: Long,
        span: GuideSpan,
    ) {
        var startMs = fromMs
        while (startMs < toMs) {
            val endMs = minOf(nextGridMark(startMs, span), toMs)
            cells += GuideCell(startMs, endMs, program = null)
            startMs = endMs
        }
    }

    /** The first 30-min mark of the span's grid strictly after [timeMs]. */
    private fun nextGridMark(
        timeMs: Long,
        span: GuideSpan,
    ): Long {
        val steps = Math.floorDiv(timeMs - span.fromMs, GuideGeometry.HALF_HOUR_MS) + 1
        return span.fromMs + steps * GuideGeometry.HALF_HOUR_MS
    }
}
