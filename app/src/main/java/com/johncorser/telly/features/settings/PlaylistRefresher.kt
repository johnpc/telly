package com.johncorser.telly.features.settings

import com.johncorser.telly.features.epg.RefreshScheduler
import com.johncorser.telly.features.playlist.db.PlaylistDao
import com.johncorser.telly.features.playlist.db.PlaylistEntity
import kotlinx.coroutines.flow.Flow

/**
 * Automatic playlist updates, mirroring [com.johncorser.telly.features.epg.EpgRefresher]:
 * each playlist carries its own "Update interval, hours" (0 = None, never
 * periodic) checked against the Room `lastUpdatedMs` stamp, and an
 * "Update on app start" toggle that forces the fetch at launch. Failures
 * are swallowed per playlist by the injected [update] (PlaylistUpdater).
 */
class PlaylistRefresher(
    private val playlistDao: PlaylistDao,
    private val update: suspend (url: String) -> Boolean,
    private val intervalHours: (url: String) -> Int,
    private val updateOnStart: (url: String) -> Boolean,
    private val clock: () -> Long,
) {
    /** Interval policy only; the minute tick calls this while running. */
    suspend fun refreshDue(): List<String> = refreshWhere(::dueByInterval)

    /** App launch: "Update on app start" playlists plus anything due. */
    suspend fun refreshOnStart(): List<String> = refreshWhere { updateOnStart(it.url) || dueByInterval(it) }

    /** MainActivity's keep-fresh loop: once at start, then per [ticks]. */
    suspend fun run(ticks: Flow<Unit>) {
        refreshOnStart()
        ticks.collect { refreshDue() }
    }

    private fun dueByInterval(playlist: PlaylistEntity): Boolean {
        val intervalMs = RefreshScheduler.hoursToMs(intervalHours(playlist.url))
        return intervalMs > RefreshScheduler.NEVER_MS && clock() - playlist.lastUpdatedMs >= intervalMs
    }

    private suspend fun refreshWhere(due: (PlaylistEntity) -> Boolean): List<String> =
        playlistDao
            .all()
            .filter(due)
            .map { it.url }
            .filter { update(it) }
}
