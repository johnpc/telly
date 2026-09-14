package com.johncorser.telly.features.history

import com.johncorser.telly.features.history.db.WatchHistoryDao
import com.johncorser.telly.features.history.db.WatchHistoryEntity
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.flow.Flow

/**
 * Watch history behind the info overlay's History card (capture 34).
 * Recorded wherever playback commits to a channel (TuneController.tune,
 * next to the lastChannelId write); deduped per channel keeping the most
 * recent watch, newest first, capped at [CAP].
 */
class WatchHistory(
    private val dao: WatchHistoryDao,
    private val clock: () -> Long,
) {
    /** Channel identity keys, most recently watched first. */
    val keys: Flow<List<String>> = dao.observeKeys()

    suspend fun record(channel: ChannelEntity) {
        dao.upsert(WatchHistoryEntity(HistoryGroup.identityOf(channel), clock()))
        dao.trimTo(CAP)
    }

    companion object {
        /**
         * The reference cap is not capturable (5.2.0 free renders no History
         * screen at all — capture 48); 30 keeps the synthetic group the size
         * of one guide page.
         */
        const val CAP = 30
    }
}
