package com.johncorser.telly.features.settings

import com.johncorser.telly.core.refresh.RefreshGate
import com.johncorser.telly.features.playlist.M3uParser
import com.johncorser.telly.features.playlist.PlaylistRepository
import kotlinx.coroutines.CancellationException

/**
 * "Update playlist" / "Update all playlists": re-runs the wizard's fetch +
 * parse + import path for an already-stored URL. Failures are swallowed per
 * playlist (the stored copy stays untouched), mirroring the EPG refresher.
 */
class PlaylistUpdater(
    private val fetchPlaylist: suspend (String) -> String,
    private val repository: PlaylistRepository,
    /** Shared with the EPG refresher: imports never overlap an EPG write. */
    private val gate: RefreshGate = RefreshGate(),
) {
    /** Returns true when the playlist was fetched and re-imported. */
    suspend fun update(sourceUrl: String): Boolean =
        gate.run("Playlist refresh", onTimeout = false) {
            runCatching { repository.add(sourceUrl, M3uParser.parse(fetchPlaylist(sourceUrl))) }
                .onFailure { if (it is CancellationException) throw it }
                .isSuccess
        }

    /** Updates every URL; returns the ones that succeeded. */
    suspend fun updateAll(sourceUrls: List<String>): List<String> = sourceUrls.filter { update(it) }
}
