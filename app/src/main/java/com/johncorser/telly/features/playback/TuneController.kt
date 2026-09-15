package com.johncorser.telly.features.playback

import com.johncorser.telly.core.kv.KeyValueStore
import com.johncorser.telly.features.history.WatchHistory
import com.johncorser.telly.features.player.PlayerEngine
import com.johncorser.telly.features.playlist.db.ChannelDao
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Owns which channel is tuned: restores the last-watched channel on start,
 * pushes streams into the [PlayerEngine], persists the last channel id plus
 * a watch-history event, and zaps with wrap-around.
 */
class TuneController(
    private val engine: PlayerEngine,
    private val store: KeyValueStore,
    private val scope: CoroutineScope,
    channelDao: ChannelDao,
    private val history: WatchHistory,
) {
    /** All visible channels in TiviMate "All channels" order. */
    val channels: StateFlow<List<ChannelEntity>> =
        channelDao.observeVisible().stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val mutableCurrent = MutableStateFlow<ChannelEntity?>(null)
    val current: StateFlow<ChannelEntity?> = mutableCurrent.asStateFlow()

    /** Tunes the last-watched channel (or the first) once channels arrive. */
    fun start() {
        scope.launch {
            val list = channels.first { it.isNotEmpty() }
            if (mutableCurrent.value == null) {
                ChannelZapper.restore(list, store.getLong(LAST_CHANNEL_KEY))?.let(::tune)
            }
        }
    }

    /** Tunes only the stored last-watched channel (guide preview resume). */
    fun resumeStored() {
        scope.launch {
            val list = channels.first { it.isNotEmpty() }
            val storedId = store.getLong(LAST_CHANNEL_KEY) ?: return@launch
            list.firstOrNull { it.id == storedId }?.let(::tune)
        }
    }

    /**
     * Tunes [channel]. A non-null [catchupUrl] plays that already-aired
     * programme stream instead of the live one: the channel is still
     * current and remembered, but no watch-history event fires (catch-up
     * is not a live watch).
     */
    fun tune(
        channel: ChannelEntity,
        catchupUrl: String? = null,
    ) {
        mutableCurrent.value = channel
        suspended = false
        engine.load(catchupUrl ?: channel.source.streamUrl)
        store.putLong(LAST_CHANNEL_KEY, channel.id)
        if (catchupUrl == null) scope.launch { history.record(channel) }
    }

    private var suspended = false

    /** Activity STOP: stop the stream like the reference does in background. */
    fun suspendPlayback() {
        if (mutableCurrent.value == null) return
        engine.stop()
        suspended = true
    }

    /** True exactly once after a background stop; the caller recovers. */
    fun consumeSuspension(): Boolean {
        val was = suspended
        suspended = false
        return was
    }

    /** Re-loads the current channel's stream after a background stop. */
    fun retune() {
        mutableCurrent.value?.let { engine.load(it.source.streamUrl) }
    }

    /** Tunes the channel [delta] steps away (wraps); false when impossible. */
    fun zap(delta: Int): Boolean {
        val next = ChannelZapper.neighbour(channels.value, mutableCurrent.value, delta) ?: return false
        tune(next)
        return true
    }

    fun byId(channelId: Long): ChannelEntity? = channels.value.firstOrNull { it.id == channelId }

    /** Retunes to the next channel when [channel] is about to disappear. */
    fun zapAwayFrom(channel: ChannelEntity) {
        if (channel.id != mutableCurrent.value?.id) return
        ChannelZapper
            .neighbour(channels.value, channel, +1)
            ?.takeIf { it.id != channel.id }
            ?.let(::tune)
    }

    fun release() = engine.release()

    companion object {
        const val LAST_CHANNEL_KEY = "lastChannelId"
    }
}
