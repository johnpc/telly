package com.johncorser.telly.features.settings

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** How long a refresh completion message stays on screen. */
internal const val REFRESH_MESSAGE_MS = 4_000L

/**
 * Runs one manual refresh action: marks its row busy (the sheet renders a
 * spinner), runs the work, then shows its completion message and clears it
 * after a beat. One refresh at a time — activations while busy are ignored.
 */
internal fun SettingsViewModel.runRefresh(
    rowId: String,
    work: suspend () -> String,
) {
    if (refreshStatus.value.busyRowId != null) return
    refreshStatus.value = RefreshStatus(busyRowId = rowId)
    scope.launch {
        val done = RefreshStatus(message = messageOf(work))
        refreshStatus.value = done
        delay(REFRESH_MESSAGE_MS)
        refreshStatus.compareAndSet(done, RefreshStatus())
    }
}

/** Defensive: a throwing refresher must never strand the spinner. */
private suspend fun messageOf(work: suspend () -> String): String =
    runCatching { work() }
        .getOrElse {
            if (it is CancellationException) throw it
            "Update failed"
        }

/** "Update playlist" (detail pane): re-fetches the one URL. */
internal suspend fun SettingsViewModel.updatePlaylistMessage(url: String): String =
    if (updater.update(url)) "Playlist updated" else "Playlist update failed"

/** "Update all playlists": every stored URL, folded into one line. */
internal suspend fun SettingsViewModel.updateAllPlaylistsMessage(): String {
    val urls = playlistItems.value.map { it.url }
    val updated = updater.updateAll(urls).size
    return when {
        updated == urls.size && urls.isNotEmpty() -> "Playlists updated"
        updated == 0 -> "Playlist update failed"
        else -> "Updated $updated of ${urls.size} playlists"
    }
}

/** "Update EPG": success = at least one playlist's sources refreshed. */
internal suspend fun SettingsViewModel.updateEpgMessage(): String =
    if (updateEpgNow() > 0) "EPG updated" else "EPG update failed"
