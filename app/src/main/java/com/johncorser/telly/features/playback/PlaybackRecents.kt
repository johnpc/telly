package com.johncorser.telly.features.playback

import com.johncorser.telly.features.history.WatchHistory
import com.johncorser.telly.features.history.db.WatchHistoryEntity
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * One recent-channel card of the info overlay's shortcut row (history-round2
 * §1): the channel logo over its CURRENT programme title in accent blue —
 * not the channel name/number; focusing it adds an air-time + title line.
 */
data class RecentCard(
    val channel: ChannelEntity,
    val nowTitle: String?,
    val nowRange: String?,
)

/** The live inputs the recent-cards row is derived from. */
class RecentRowSources(
    val channels: StateFlow<List<ChannelEntity>>,
    val events: Flow<List<WatchHistoryEntity>>,
    val current: StateFlow<ChannelEntity?>,
    val instant: Flow<Long>,
)

/** Wires the feed from the playback ViewModel's live parts. */
fun recentRowFeed(
    env: PlaybackEnv,
    tuner: TuneController,
    history: WatchHistory,
    instant: Flow<Long>,
    scope: CoroutineScope,
): RecentRowFeed =
    RecentRowFeed(
        RecentRowSources(tuner.channels, history.events, tuner.current, instant),
        env.epgRepository,
        env.time.zone,
        scope,
    )

/**
 * Actions of the info-row recent cards. Deliberate deviation (charter
 * precedent, cf. favorites/add-source): the free reference opens Unlock
 * Premium on a recent card (history-round2 §2); telly has no premium tier,
 * so OK TUNES the card's channel. Clear empties THE watch_history table —
 * telly feeds the recent row and the History screen from one table where
 * the reference keeps separate sources (documented simplification).
 */
class PlaybackRecents(
    private val feed: RecentRowFeed,
    private val history: WatchHistory,
    private val scope: CoroutineScope,
    private val tuneAndZap: (RecentCard) -> Unit,
    private val keepAlive: () -> Unit,
) {
    val cards: StateFlow<List<RecentCard>> get() = feed.cards

    fun tune(card: RecentCard) = tuneAndZap(card)

    fun clear() {
        scope.launch { history.clear() }
        keepAlive()
    }
}
