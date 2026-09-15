package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.Setting
import com.johncorser.telly.features.recording.recordingRows
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The active sheet's rows; the Recording pane needs the DVR storage hook. */
internal fun SettingsViewModel.activeRows(
    pane: SettingsPane?,
    playlists: List<PlaylistItem>,
    feeds: SettingsFeeds,
): List<SettingsRow> =
    if (pane == SettingsPane.Recording) {
        recordingRows(recordings)
    } else {
        rowsFor(pane, settings, playlists, versionName, feeds)
    }

/** Rebuild triggers beyond the feeds: settings edits + the manual bump. */
internal fun SettingsViewModel.rowTicks(): Flow<Unit> = merge(settings.changes.map { }, refresh.map { })

/** State-mutation helpers shared by the dispatch extension files. */

internal fun SettingsViewModel.flip(setting: Setting<Boolean>) {
    settings.set(setting, !settings.get(setting))
}

internal fun SettingsViewModel.push(pane: SettingsPane) {
    mutableState.update { it.copy(panes = it.panes + pane) }
}

internal fun SettingsViewModel.showOverlay(overlay: SettingsOverlay) {
    mutableState.update { it.copy(overlay = overlay) }
}

internal fun SettingsViewModel.currentDetailUrl(): String? =
    (mutableState.value.activePane as? SettingsPane.PlaylistDetail)?.url

internal fun SettingsViewModel.popDetailPane() {
    mutableState.update { state ->
        state.copy(panes = state.panes.filterNot { it is SettingsPane.PlaylistDetail })
    }
}

/** Enabling parental controls without a PIN forces PIN setup first. */
internal fun SettingsViewModel.toggleParentalMaster() {
    val enabling = !parental.isEnabled
    parental.setEnabled(enabling)
    if (enabling && !parental.hasPin) showOverlay(SettingsOverlay.PinSetup)
}

internal fun SettingsViewModel.launch(block: suspend () -> Unit) {
    scope.launch { block() }
}
