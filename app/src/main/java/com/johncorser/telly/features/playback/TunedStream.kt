package com.johncorser.telly.features.playback

import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * What the tuner has put on the engine — the ONE owner of the live /
 * archive / external mode (docs/state-machines.md). Every surface that used
 * to infer "are we in catch-up?" or "did we hand off?" from side flags reads
 * this instead: [TuneController.tune] publishes exactly one value per tune
 * that passed the blocked-channel gate, so a gated or cancelled tune never
 * changes the mode.
 */
sealed interface TunedStream {
    val channel: ChannelEntity

    /** The channel's live stream plays on the internal engine. */
    data class Live(
        override val channel: ChannelEntity,
    ) : TunedStream

    /** An already-aired programme's archive ([url]) plays on the internal engine. */
    data class Archive(
        override val channel: ChannelEntity,
        val url: String,
    ) : TunedStream

    /** The live stream was handed to the external player; the engine is stopped. */
    data class External(
        override val channel: ChannelEntity,
    ) : TunedStream
}

/** The tuner's ONE write site for the mode and its channel projection. */
class TunedStreamState {
    private val mutablePlaying = MutableStateFlow<TunedStream?>(null)
    private val mutableCurrent = MutableStateFlow<ChannelEntity?>(null)

    val playing: StateFlow<TunedStream?> = mutablePlaying.asStateFlow()
    val current: StateFlow<ChannelEntity?> = mutableCurrent.asStateFlow()

    fun publish(stream: TunedStream) {
        mutableCurrent.value = stream.channel
        mutablePlaying.value = stream
    }
}
