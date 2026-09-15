package com.johncorser.telly.features.guide

import com.johncorser.telly.features.epg.ProgramTitle
import com.johncorser.telly.features.playback.ProgramTimes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.TimeZone

/** Everything the guide's top-right info pane renders (uidump 24). */
data class GuideInfoData(
    val title: String,
    val range: String,
    val remaining: String?,
    val progressPermille: Int?,
    val description: String?,
    val group: String?,
    val favorite: Boolean,
)

object GuideInfoBuilder {
    /** TiviMate's placeholder for EPG-less cells and channels. */
    const val NO_INFORMATION = "No information"

    /** Live pane state: follows focus, rows and the minute-ticked "now". */
    fun feed(
        rows: Flow<List<GuideRow>>,
        focus: Flow<GuideFocus?>,
        now: Flow<Long>,
        zone: TimeZone,
        scope: CoroutineScope,
    ): StateFlow<GuideInfoData?> =
        combine(rows, focus, now) { list, focused, at -> buildFor(list, focused, at, zone) }
            .stateIn(scope, SharingStarted.Eagerly, null)

    /** The pane for the focused cell of [rows]; null with nothing focused. */
    fun buildFor(
        rows: List<GuideRow>,
        focused: GuideFocus?,
        nowMs: Long,
        zone: TimeZone,
    ): GuideInfoData? {
        val row = focused?.let { rows.getOrNull(it.rowIndex) } ?: return null
        return build(row, focused.cell, nowMs, zone)
    }

    /**
     * Title, "02:30 — 03:45 PM", remaining minutes + progress (airing cells
     * only, per capture 24), description and the channel's group name.
     */
    fun build(
        row: GuideRow,
        cell: GuideCell,
        nowMs: Long,
        zone: TimeZone,
    ): GuideInfoData {
        val airing = cell.contains(nowMs)
        return GuideInfoData(
            title = cell.program?.details?.let(ProgramTitle::of) ?: NO_INFORMATION,
            range = ProgramTimes.range(cell.startMs, cell.endMs, zone),
            remaining = if (airing) "${ProgramTimes.remainingMinutes(cell.endMs, nowMs)} min" else null,
            progressPermille = if (airing) ProgramTimes.progressPermille(cell.startMs, cell.endMs, nowMs) else null,
            description = cell.program?.details?.description,
            group = row.channel.source.groupTitle,
            favorite = row.channel.flags.favorite,
        )
    }
}
