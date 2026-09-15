package com.johncorser.telly.features.settings

import com.johncorser.telly.features.epg.EpgSource
import kotlinx.coroutines.flow.update
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Custom EPG source management (Settings -> EPG -> EPG sources). The
 * reference free tier locks these behind premium; telly ships them by
 * product directive, shaped after TiviMate's documented flow (ux-spec
 * 3.10: add a source URL, edit/delete per source). URLs are validated
 * like the wizard's (http/https); invalid input is ignored on commit.
 */

internal fun SettingsViewModel.editEpgSourceUrlOverlay() {
    val source = currentEpgSource() ?: return
    showOverlay(SettingsOverlay.TextEdit(rowId = RowIds.EPG_SOURCE_URL, title = "Source URL", value = source.url))
}

internal fun SettingsViewModel.confirmDeleteEpgSourceOverlay() {
    val source = currentEpgSource() ?: return
    showOverlay(SettingsOverlay.ConfirmDeleteSource(sourceId = source.id, name = source.name))
}

/** Add commits attach to the enclosing playlist pane, else the first playlist. */
internal fun SettingsViewModel.addEpgSource(rawUrl: String) {
    val url = validEpgSourceUrl(rawUrl) ?: return
    val playlistUrl = enclosingPlaylistUrl() ?: return
    launch { epgSources.add(playlistUrl, url) }
}

internal fun SettingsViewModel.setEpgSourceUrl(rawUrl: String) {
    val url = validEpgSourceUrl(rawUrl) ?: return
    val source = currentEpgSource() ?: return
    launch { epgSources.setUrl(source.id, url) }
}

/** OK on Delete in the source confirm: remove the source and pop its pane. */
fun SettingsViewModel.confirmDeleteEpgSource() {
    val confirm = state.value.overlay as? SettingsOverlay.ConfirmDeleteSource ?: return
    launch { epgSources.remove(confirm.sourceId) }
    mutableState.update { state ->
        state.copy(panes = state.panes.filterNot { it is SettingsPane.EpgSourceDetail })
    }
    dismissOverlay()
}

internal fun SettingsViewModel.currentEpgSource(): EpgSource? {
    val pane = mutableState.value.activePane as? SettingsPane.EpgSourceDetail ?: return null
    return epgSourceItems.value.firstOrNull { it.id == pane.sourceId }
}

private fun SettingsViewModel.enclosingPlaylistUrl(): String? =
    mutableState.value.panes
        .filterIsInstance<SettingsPane.PlaylistDetail>()
        .lastOrNull()
        ?.url ?: playlistItems.value.firstOrNull()?.url

private fun validEpgSourceUrl(raw: String): String? = raw.trim().takeIf { it.toHttpUrlOrNull() != null }
