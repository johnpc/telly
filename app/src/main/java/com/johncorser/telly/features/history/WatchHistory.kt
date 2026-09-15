package com.johncorser.telly.features.history

import com.johncorser.telly.features.history.db.WatchHistoryDao
import com.johncorser.telly.features.history.db.WatchHistoryEntity
import com.johncorser.telly.features.playlist.ChannelImporter
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.flow.Flow

/**
 * Watch history behind the info overlay's recent-channel cards and the
 * History screen (history-round2; ONE table feeds both — a documented
 * simplification, the reference keeps separate sources). Recorded wherever
 * playback commits to a channel (TuneController.tune, next to the
 * lastChannelId write); deduped per channel keeping the most recent watch,
 * newest first, capped at [CAP].
 */
class WatchHistory(
    private val dao: WatchHistoryDao,
    private val clock: () -> Long,
) {
    /** Watch events, most recently watched first. */
    val events: Flow<List<WatchHistoryEntity>> = dao.observeEvents()

    suspend fun record(channel: ChannelEntity) {
        dao.upsert(WatchHistoryEntity(identityOf(channel), clock()))
        dao.trimTo(CAP)
    }

    /** Clear card + the History screen's clear-all empty the one table. */
    suspend fun clear() = dao.clear()

    companion object {
        /**
         * The reference cap is not capturable (its populated History screen
         * could not be reached — history-round2 §3); 30 keeps the surfaces
         * one screenful.
         */
        const val CAP = 30

        /** Playlist-refresh-stable channel identity, same key as user flags. */
        fun identityOf(channel: ChannelEntity): String =
            ChannelImporter.identityOf(channel.source.tvgId, channel.source.streamUrl, channel.source.name)
    }
}
