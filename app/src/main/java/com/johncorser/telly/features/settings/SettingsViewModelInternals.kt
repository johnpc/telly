package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.Setting
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** State-mutation helpers shared by the dispatch extension files. */

internal fun SettingsViewModel.flip(setting: Setting<Boolean>) {
    settings.set(setting, !settings.get(setting))
}

internal fun SettingsViewModel.push(pane: SettingsPane) {
    mutableState.update { it.copy(subPanes = it.subPanes + pane) }
}

internal fun SettingsViewModel.showOverlay(overlay: SettingsOverlay) {
    mutableState.update { it.copy(overlay = overlay) }
}

internal fun SettingsViewModel.currentDetailUrl(): String? =
    (mutableState.value.activePane as? SettingsPane.PlaylistDetail)?.url

internal fun SettingsViewModel.popDetailPane() {
    mutableState.update { state ->
        state.copy(subPanes = state.subPanes.filterNot { it is SettingsPane.PlaylistDetail })
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
