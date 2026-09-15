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

    /** Branded placeholder for unbuilt rows; BACK returns to [back]. */
    data class ComingSoon(
        val feature: String,
        val back: GuideLayer = Grid,
    ) : GuideLayer

    /** The sheet's working "Program description" row. */
    data class Description(
        val title: String,
        val text: String,
        val back: GuideLayer = RowMenu,
    ) : GuideLayer

    /** "Channel options" pane, every row premium-locked (captures 41-42); it REPLACES the sheet, BACK → grid. */
    data class ChannelOptions(
        val channelName: String,
    ) : GuideLayer

    /** "Stop recording?" GuidedStep from a second Record (recording slice). */
    data class RecordingStop(
        val recordingId: Long,
        val channelName: String,
        val back: GuideLayer = Grid,
    ) : GuideLayer

    /** The custom-recording form; BACK returns to the invoking layer. */
    data class CustomRecording(
        val back: GuideLayer = Grid,
    ) : GuideLayer
}

/** The sheet's Program-description layer from the focused programme. */
internal fun descriptionLayer(data: GuideInfoData?): GuideLayer.Description =
    GuideLayer.Description(
        title = data?.title ?: GuideInfoBuilder.NO_INFORMATION,
        text = data?.description ?: GuideInfoBuilder.NO_INFORMATION,
    )

/**
 * One BACK level per layer: pushed screens → sheet, everything else → grid.
 * Channel options is NOT a pushed screen: in the reference it replaces the
 * sheet (in-place cross-fade), so one BACK from it lands on the grid with
 * the originating row's focus restored by [GuideFocusMemory] (ref-round6
 * §A — the reference never returns to the sheet).
 */
internal fun backOf(layer: GuideLayer): GuideLayer =
    when (layer) {
        is GuideLayer.ComingSoon -> layer.back
        is GuideLayer.Description -> layer.back
        is GuideLayer.RecordingStop -> layer.back
        is GuideLayer.CustomRecording -> layer.back
        else -> GuideLayer.Grid
    }

/** D-pad keys the guide reacts to (mapped from KeyEvents in the UI). */
enum class GuideKey { OK, LONG_OK, MENU, BACK, UP, DOWN, LEFT, RIGHT, LONG_LEFT, LONG_RIGHT, CHANNEL_UP, CHANNEL_DOWN }

/**
 * The five rows of the future-cell dropdown, verbatim from capture 27.
 * Record / Custom recording are live (recording slice); the rest open the
 * branded coming-soon placeholder until their slices ship.
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
