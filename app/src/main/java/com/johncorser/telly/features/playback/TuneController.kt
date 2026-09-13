package com.johncorser.telly.features.playback

import com.johncorser.telly.core.kv.KeyValueStore
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
 * pushes streams into the [PlayerEngine], persists the last channel id, and
 * zaps with wrap-around.
 */
class TuneController(
    private val engine: PlayerEngine,
    private val store: KeyValueStore,
    private val scope: CoroutineScope,
    channelDao: ChannelDao,
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

    fun tune(channel: ChannelEntity) {
        mutableCurrent.value = channel
        engine.load(channel.source.streamUrl)
        store.putLong(LAST_CHANNEL_KEY, channel.id)
    }

    /** Tunes the channel [delta] steps away (wraps); false when impossible. */
    fun zap(delta: Int): Boolean {
        val next = neighbour(delta) ?: return false
        tune(next)
        return true
    }

    fun neighbour(offset: Int): ChannelEntity? = ChannelZapper.neighbour(channels.value, mutableCurrent.value, offset)

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
