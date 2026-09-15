package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.TellySettings

/**
 * Non-generic row activations: everything that is not a plain toggle or
 * picker. Split from SettingsViewModel to keep files small (CLAUDE.md gate).
 */
internal fun SettingsViewModel.runAction(rowId: String) {
    when (rowId) {
        RowIds.EPG_SOURCES, RowIds.PLAYLIST_EPG_SOURCES -> push(SettingsPane.EpgSources)
        RowIds.EPG_UPDATE_NOW -> launch { updateEpgNow() }
        RowIds.EPG_ADD_SOURCE -> showOverlay(SettingsOverlay.TextEdit(rowId, title = "EPG URL", value = ""))
        RowIds.EPG_SOURCE_URL -> editEpgSourceUrlOverlay()
        RowIds.EPG_SOURCE_DELETE -> confirmDeleteEpgSourceOverlay()
        RowIds.USER_AGENT -> textEditOverlay(rowId, "User-Agent", settings.get(TellySettings.USER_AGENT))
        RowIds.UDP_PROXY -> textEditOverlay(rowId, "UDP proxy (address:port)", settings.get(TellySettings.UDP_PROXY))
        RowIds.BACK_UP_DATA ->
            launch {
                backup.exportLocal()
                callbacks.onExportBackup(backup.exportJson())
            }
        RowIds.RESTORE_DATA -> callbacks.onImportBackup()
        RowIds.PARENTAL_CHANGE_PIN -> showOverlay(SettingsOverlay.PinSetup)
        RowIds.ABOUT_PRIVACY_POLICY -> callbacks.onOpenPrivacyPolicy()
        else -> runPlaylistAction(rowId)
    }
}

/** Playlist-list, per-playlist and recording-pane actions. */
private fun SettingsViewModel.runPlaylistAction(rowId: String) {
    when (rowId) {
        RowIds.ADD_PLAYLIST -> addPlaylist()
        RowIds.UPDATE_ALL_PLAYLISTS -> launch { updater.updateAll(playlistItems.value.map { it.url }) }
        RowIds.PLAYLIST_UPDATE_NOW -> currentDetailUrl()?.let { url -> launch { updater.update(url) } }
        RowIds.PLAYLIST_ENABLE -> currentDetailUrl()?.let { flip(playlistEnabledSetting(it)) }
        RowIds.PLAYLIST_DELETE -> confirmDeleteOverlay()
        RowIds.PLAYLIST_NAME -> renameOverlay()
        RowIds.OTHER_RECORDING -> push(SettingsPane.Recording)
        RowIds.RECORDING_DELETE_ALL -> showOverlay(SettingsOverlay.ConfirmDeleteRecordings)
    }
}

/** Multiple playlists are allowed; adding always opens the wizard. */
internal fun SettingsViewModel.addPlaylist() {
    callbacks.onAddPlaylist()
}

private fun SettingsViewModel.confirmDeleteOverlay() {
    val item = currentDetailItem() ?: return
    showOverlay(SettingsOverlay.ConfirmDelete(url = item.url, name = item.name))
}

private fun SettingsViewModel.renameOverlay() {
    val item = currentDetailItem() ?: return
    showOverlay(SettingsOverlay.TextEdit(rowId = RowIds.PLAYLIST_NAME, title = "Playlist name", value = item.name))
}

private fun SettingsViewModel.textEditOverlay(
    rowId: String,
    title: String,
    value: String,
) {
    showOverlay(SettingsOverlay.TextEdit(rowId = rowId, title = title, value = value))
}

internal fun SettingsViewModel.currentDetailItem(): PlaylistItem? =
    currentDetailUrl()?.let { url -> playlistItems.value.firstOrNull { it.url == url } }
