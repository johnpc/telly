package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.TellySettings
import kotlinx.coroutines.flow.update

// Overlay results (picker choice, text commits, delete confirm, PIN) + the
// sheet BACK chain.

/** BACK inside settings: overlay first, then one sheet. False = leave. */
fun SettingsViewModel.back(): Boolean {
    val current = mutableState.value
    return when {
        current.overlay != null -> {
            dismissOverlay()
            true
        }
        current.panes.isNotEmpty() -> {
            mutableState.update { it.copy(panes = it.panes.dropLast(1)) }
            true
        }
        else -> false
    }
}

fun SettingsViewModel.dismissOverlay() {
    mutableState.update { it.copy(overlay = null) }
}

fun SettingsViewModel.choosePickerOption(raw: String) {
    val picker = state.value.overlay as? SettingsOverlay.Picker ?: return
    settings.writeRaw(picker.spec.key, raw)
    dismissOverlay()
}

fun SettingsViewModel.submitText(value: String) {
    val edit = state.value.overlay as? SettingsOverlay.TextEdit ?: return
    when (edit.rowId) {
        RowIds.PLAYLIST_NAME ->
            currentDetailUrl()?.let { url ->
                launch { playlistRepository.rename(url, value.trim().ifEmpty { url }) }
            }
        RowIds.USER_AGENT -> settings.set(TellySettings.USER_AGENT, value.trim())
        RowIds.UDP_PROXY -> settings.set(TellySettings.UDP_PROXY, value.trim())
        RowIds.EPG_ADD_SOURCE -> addEpgSource(value)
        RowIds.EPG_SOURCE_URL -> setEpgSourceUrl(value)
        RowIds.PLAYLIST_USER_AGENT -> submitPlaylistUserAgent(value)
        RowIds.PLAYLIST_URL -> submitPlaylistUrl(value)
    }
    dismissOverlay()
}

/** OK on Delete in the captured GuidedStep confirm (screen 22). */
fun SettingsViewModel.confirmDelete() {
    val confirm = state.value.overlay as? SettingsOverlay.ConfirmDelete ?: return
    launch { playlistRepository.delete(confirm.url) }
    popDetailPane()
    dismissOverlay()
}

/** Commits a new parental PIN (salted + hashed; never stored raw). */
fun SettingsViewModel.submitPin(pin: String) {
    if (pin.isNotEmpty()) parental.setPin(pin)
    dismissOverlay()
}

/** Restores a backup JSON picked via SAF; no-op for foreign files. */
fun SettingsViewModel.importBackup(json: String) {
    launch { backup.importJson(json) }
}
