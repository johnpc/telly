package com.johncorser.telly.features.multiview

import com.johncorser.telly.features.epg.ProgramTitle
import com.johncorser.telly.features.epg.db.ProgramEntity
import com.johncorser.telly.features.playback.ClockStyle
import com.johncorser.telly.features.playback.ProgramTimes

/** One schedule row of the picker's middle pane: "12:45 AM  Title: Sub". */
data class MultiviewScheduleRow(
    val timeText: String,
    val title: String,
    val airing: Boolean,
)

/**
 * Pure schedule assembly for the picker's middle pane (multiview-round 05:
 * time + programme rows for the focused channel, chronological, reaching a
 * few hours into the past). The exact window anchor is not capturable in
 * the free build; telly shows 6 h back to 18 h ahead.
 */
object MultiviewSchedule {
    private const val HOUR_MS = 3_600_000L
    const val PAST_MS = 6 * HOUR_MS
    const val FUTURE_MS = 18 * HOUR_MS

    fun build(
        programs: List<ProgramEntity>,
        atMs: Long,
        style: ClockStyle,
    ): List<MultiviewScheduleRow> =
        programs.sortedBy { it.startMs }.map { program ->
            MultiviewScheduleRow(
                timeText = ProgramTimes.startTime(program.startMs, style),
                title = ProgramTitle.of(program.details),
                airing = atMs >= program.startMs && atMs < program.endMs,
            )
        }
}
