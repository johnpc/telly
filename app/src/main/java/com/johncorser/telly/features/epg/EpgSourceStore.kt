package com.johncorser.telly.features.epg

import com.johncorser.telly.features.epg.db.EpgSourceDao
import com.johncorser.telly.features.epg.db.EpgSourceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** One custom EPG source, attached to the playlist stored at [playlistUrl]. */
data class EpgSource(
    val id: Long,
    val playlistUrl: String,
    val url: String,
) {
    /** Display name: the URL's host, like the reference names its sources. */
    val name: String get() = hostOf(url)

    private companion object {
        fun hostOf(url: String): String =
            url
                .substringAfter("://", url)
                .substringBefore('/')
                .substringBefore(':')
                .ifBlank { url }
    }
}

/**
 * Storage boundary for custom EPG sources (Settings -> EPG -> EPG sources).
 * The auto-detected source (the playlist's `url-tvg`) is NOT stored here —
 * it lives on the playlist row and is refreshed with it.
 */
interface EpgSourceStore {
    /** Every custom source, in the order it was added. */
    val sources: Flow<List<EpgSource>>

    /** The custom sources of one playlist, in fetch (added) order. */
    suspend fun forPlaylist(playlistUrl: String): List<EpgSource>

    /** Adds [url] for the playlist; re-adding the same URL is a no-op swap. */
    suspend fun add(
        playlistUrl: String,
        url: String,
    )

    /** Rewrites the URL of the source [id] (settings: edit source). */
    suspend fun setUrl(
        id: Long,
        url: String,
    )

    /** Deletes the source [id] (settings: delete source). */
    suspend fun remove(id: Long)
}

/** Room-backed store; timestamps come from the injected [clock]. */
class RoomEpgSourceStore(
    private val dao: EpgSourceDao,
    private val clock: () -> Long,
) : EpgSourceStore {
    override val sources: Flow<List<EpgSource>> =
        dao.observeAll().map { rows -> rows.map { it.toSource() } }

    override suspend fun forPlaylist(playlistUrl: String): List<EpgSource> =
        dao.forPlaylist(playlistUrl).map { it.toSource() }

    override suspend fun add(
        playlistUrl: String,
        url: String,
    ) {
        dao.upsert(EpgSourceEntity(playlistUrl = playlistUrl, url = url, addedAtMs = clock()))
    }

    override suspend fun setUrl(
        id: Long,
        url: String,
    ) = dao.setUrl(id, url)

    override suspend fun remove(id: Long) = dao.delete(id)

    private fun EpgSourceEntity.toSource(): EpgSource = EpgSource(id = id, playlistUrl = playlistUrl, url = url)
}
