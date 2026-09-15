package com.johncorser.telly.features.vod

import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.settings.RowIds
import com.johncorser.telly.features.settings.SettingsRow
import com.johncorser.telly.features.settings.SettingsViewModel
import com.johncorser.telly.features.settings.dismissOverlay
import com.johncorser.telly.features.settings.launch

/**
 * Settings -> Other -> VOD (telly's unlocked pane; the reference sells VOD
 * as premium, so the pane's rows are designed, not cloned): the resume
 * toggle plus the clear-positions action behind a GuidedStep confirm.
 */
fun vodSettingsRows(settings: SettingsRepository): List<SettingsRow> =
    listOf(
        SettingsRow.Toggle(
            id = RowIds.VOD_REMEMBER_POSITION,
            title = "Remember playback position",
            checked = settings.get(TellySettings.VOD_REMEMBER_POSITION),
        ),
        SettingsRow.Action(id = RowIds.VOD_CLEAR_POSITIONS, title = "Clear playback positions"),
    )

/** OK on Clear in the confirm step: empty vod_positions, close the dialog. */
fun SettingsViewModel.confirmClearVodPositions() {
    launch { clearVodPositions() }
    dismissOverlay()
}
