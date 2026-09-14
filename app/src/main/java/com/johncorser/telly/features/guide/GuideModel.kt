package com.johncorser.telly.features.guide

import com.johncorser.telly.features.epg.db.ProgramEntity
import com.johncorser.telly.features.playlist.db.ChannelEntity

/**
 * One programme cell of the grid. A null [program] is a TiviMate-style
 * "No information" filler covering an EPG gap.
 */
data class GuideCell(
    val startMs: Long,
    val endMs: Long,
    val program: ProgramEntity?,
) {
    val hasInfo: Boolean get() = program != null

    fun contains(atMs: Long): Boolean = atMs in startMs until endMs
}

/**
 * One channel row: playlist number in "All channels", renumbered from 1
 * inside a group (capture 74), plus the materialized cells of the window.
 */
data class GuideRow(
    val channel: ChannelEntity,
    val displayNumber: Int,
    val cells: List<GuideCell>,
)

/** The focused cell plus the time anchor UP/DOWN try to keep constant. */
data class GuideFocus(
    val rowIndex: Int,
    val cell: GuideCell,
    val anchorMs: Long,
)

/** Which interaction layer owns the D-pad on the guide screen. */
sealed interface GuideLayer {
    /** The grid itself (default). */
    data object Grid : GuideLayer

    /** Groups column slid in at the left (capture 25). */
    data object Groups : GuideLayer

    /** Dropdown anchored under a non-airing cell (capture 27). */
    data class CellMenu(
        val cell: GuideCell,
    ) : GuideLayer

    /** Long-OK/MENU row context sheet over the grid (round3-ref 05, 38-40). */
    data object RowMenu : GuideLayer

    /** Unlock Premium screen (capture 28/31); BACK returns to [back]. */
    data class Paywall(
        val feature: String,
        val back: GuideLayer = Grid,
    ) : GuideLayer

    /** Branded placeholder for sheet rows whose feature is a later slice. */
    data class ComingSoon(
        val feature: String,
    ) : GuideLayer

    /** The sheet's working "Program description" row. */
    data class Description(
        val title: String,
        val text: String,
    ) : GuideLayer

    /** "Channel options" pane, every row premium-locked (captures 41-42); it REPLACES the sheet, BACK → grid. */
    data class ChannelOptions(
        val channelName: String,
    ) : GuideLayer
}

/** D-pad keys the guide reacts to (mapped from KeyEvents in the UI). */
enum class GuideKey { OK, LONG_OK, MENU, BACK, UP, DOWN, LEFT, RIGHT, LONG_LEFT, LONG_RIGHT }

/**
 * The five rows of the future-cell dropdown, verbatim from capture 27.
 * All premium-gated in the free reference build (capture 31): each opens
 * the Unlock Premium screen.
 */
enum class GuideCellAction(
    val label: String,
) {
    REMIND("Remind"),
    RECORD("Record"),
    CUSTOM_RECORDING("Custom recording"),
    ADD_TO_MY_LIST("Add to My list"),
    PROGRAM_DESCRIPTION("Program description"),
}
