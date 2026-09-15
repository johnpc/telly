package com.johncorser.telly.features.playlist

import androidx.room.withTransaction
import com.johncorser.telly.core.db.TellyDatabase
import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.playlist.db.PlaylistEntity
import com.johncorser.telly.features.vod.VodClassifier
import com.johncorser.telly.features.vod.VodImporter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed playlist store. Imports run through [ChannelImporter], so
 * channel numbers follow playlist order and favorite/hidden flags survive
 * re-adds of the same URL. All timestamps come from the injected [clock].
 */
class RoomPlaylistRepository(
    private val database: TellyDatabase,
    private val clock: () -> Long,
) : PlaylistRepository {
    private val playlistDao = database.playlistDao()
    private val channelDao = database.channelDao()
    private val vodItemDao = database.vodItemDao()

    override val playlists: Flow<List<StoredPlaylist>> =
        playlistDao.observeAll().map { rows -> rows.map { toStored(it) } }

    override suspend fun add(
        sourceUrl: String,
        playlist: M3uPlaylist,
        name: String?,
    ) {
        database.withTransaction {
            // TiviMate classification: video-file entries are VOD "Movies",
            // never guide channels (they'd pollute the guide/panel/search).
            val (vod, live) = playlist.channels.partition { VodClassifier.isVod(it.streamUrl) }
            val existing = playlistDao.byUrl(sourceUrl)
            val previousChannels = existing?.let { channelDao.forPlaylist(it.id) }.orEmpty()
            val playlistId =
                playlistDao.upsert(
                    PlaylistEntity(
                        id = existing?.id ?: 0,
                        name = name ?: existing?.name ?: nameFor(sourceUrl),
                        url = sourceUrl,
                        epgUrl = playlist.epgUrl,
                        lastUpdatedMs = clock(),
                        epgLastUpdatedMs = existing?.epgLastUpdatedMs ?: 0,
                    ),
                )
            channelDao.deleteForPlaylist(playlistId)
            channelDao.insertAll(ChannelImporter.import(playlistId, live, previousChannels))
            vodItemDao.deleteForPlaylist(playlistId)
            vodItemDao.insertAll(VodImporter.import(playlistId, vod))
        }
    }

    override suspend fun rename(
        sourceUrl: String,
        name: String,
    ) {
        playlistDao.rename(sourceUrl, name)
    }

    override suspend fun changeUrl(
        oldUrl: String,
        newUrl: String,
    ): Boolean =
        database.withTransaction {
            val movable = playlistDao.byUrl(newUrl) == null && playlistDao.byUrl(oldUrl) != null
            if (movable) playlistDao.updateUrl(oldUrl, newUrl)
            movable
        }

    override suspend fun delete(sourceUrl: String) {
        database.withTransaction {
            val existing = playlistDao.byUrl(sourceUrl) ?: return@withTransaction
            channelDao.deleteForPlaylist(existing.id)
            playlistDao.delete(existing.id)
        }
    }

    private suspend fun toStored(row: PlaylistEntity): StoredPlaylist =
        StoredPlaylist(
            sourceUrl = row.url,
            name = row.name,
            playlist =
                M3uPlaylist(
                    epgUrl = row.epgUrl,
                    channels = channelDao.forPlaylist(row.id).map { toM3uChannel(it) },
                ),
        )

    private fun toM3uChannel(row: ChannelEntity): M3uChannel =
        M3uChannel(
            title = row.source.name,
            streamUrl = row.source.streamUrl,
            tvgId = row.source.tvgId,
            tvgName = null,
            tvgLogo = row.source.logoUrl,
            groupTitle = row.source.groupTitle,
        )

    /** A human-readable default playlist name: the URL's last path segment. */
    private fun nameFor(sourceUrl: String): String =
        sourceUrl
            .substringBefore('?')
            .trimEnd('/')
            .substringAfterLast('/')
            .ifBlank { sourceUrl }
}
