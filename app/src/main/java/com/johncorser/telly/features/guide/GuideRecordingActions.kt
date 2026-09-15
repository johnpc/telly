package com.johncorser.telly.features.guide

import com.johncorser.telly.features.epg.ProgramTitle
import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.recording.RecordingMenu

/**
 * The guide's Record / Custom recording actions, split from
 * [GuideMenuController] (file-size gate): resolves the focused channel /
 * cell and forwards to the DVR menu, falling back to the coming-soon
 * placeholder while no recording center is wired (JVM tests).
 */
internal class GuideRecordingActions(
    private val recording: () -> RecordingMenu?,
    private val focusedRow: () -> GuideRow?,
    private val show: (GuideLayer) -> Unit,
) {
    /** The sheet's Record row: instant record of the row's channel. */
    fun record(channel: ChannelEntity) {
        withMenu(GuideCellAction.RECORD.label) { it.onRecord(channel) }
    }

    /** Cell-dropdown Record: schedule the focused future programme's slot. */
    fun recordCell(layer: GuideLayer) {
        val cell = (layer as? GuideLayer.CellMenu)?.cell ?: return
        val channel = focusedRow()?.channel ?: return
        withMenu(GuideCellAction.RECORD.label) {
            it.onRecordProgramme(
                channel = channel,
                title = cell.program?.let { program -> ProgramTitle.of(program.details) },
                startMs = cell.startMs,
                endMs = cell.endMs,
            )
        }
    }

    /** Both the sheet row and the cell dropdown open the same form. */
    fun customRecording() {
        val channel = focusedRow()?.channel ?: return
        withMenu(GuideCellAction.CUSTOM_RECORDING.label) { it.onCustomRecording(channel) }
    }

    private fun withMenu(
        fallbackLabel: String,
        action: (RecordingMenu) -> Unit,
    ) {
        val menu = recording()
        if (menu == null) show(GuideLayer.ComingSoon(fallbackLabel)) else action(menu)
    }
}
