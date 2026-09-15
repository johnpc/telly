package com.johncorser.telly.features.playlist

import kotlinx.coroutines.CancellationException

/**
 * Settings -> playlist -> "Playlist URL": moves a stored playlist to a new
 * URL. The new URL is fetched and parsed FIRST — a failure keeps the old
 * URL untouched, the [PlaylistUpdater] idiom — then everything keyed by
 * the old URL is re-keyed (the Room row in place, so channels and their
 * favorite/hidden flags survive; the per-playlist KV settings; the
 * playlist's custom EPG sources) and the fresh parse is imported.
 */
class PlaylistUrlChanger(
    private val fetchPlaylist: suspend (String) -> String,
    private val repository: PlaylistRepository,
    private val rekeySettings: (oldUrl: String, newUrl: String) -> Unit,
    private val rekeyEpgSources: suspend (oldUrl: String, newUrl: String) -> Unit,
) {
    /** True when the playlist now lives at [newUrl]; false keeps [oldUrl]. */
    suspend fun change(
        oldUrl: String,
        newUrl: String,
    ): Boolean {
        val parsed =
            runCatching { M3uParser.parse(fetchPlaylist(newUrl)) }
                .onFailure { if (it is CancellationException) throw it }
                .getOrNull()
        return parsed != null && rekey(oldUrl, newUrl, parsed)
    }

    private suspend fun rekey(
        oldUrl: String,
        newUrl: String,
        parsed: M3uPlaylist,
    ): Boolean {
        if (!repository.changeUrl(oldUrl, newUrl)) return false
        rekeySettings(oldUrl, newUrl)
        rekeyEpgSources(oldUrl, newUrl)
        repository.add(newUrl, parsed)
        return true
    }
}
