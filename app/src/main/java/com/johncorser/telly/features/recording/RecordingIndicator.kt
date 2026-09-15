package com.johncorser.telly.features.recording

import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.recording.db.RecordingEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

/**
 * Derives the transport record dot's live state: red while the CURRENT
 * channel has an in-progress recording, grey otherwise. Pure derivation
 * over the DVR library feed — no clock, no Room.
 */
object RecordingIndicator {
    /** True when [channel] is being recorded right now. */
    fun isRecording(
        recordings: List<RecordingEntity>,
        channel: ChannelEntity?,
    ): Boolean {
        val key = channel?.let(RecordingCenter::keyOf) ?: return false
        return recordings.any { it.status == RecordingStatus.RECORDING.name && it.channelKey == key }
    }

    /** The dot state over the library feed (null feed = no DVR = never red). */
    fun activeFlow(
        recordings: Flow<List<RecordingEntity>>?,
        current: Flow<ChannelEntity?>,
        scope: CoroutineScope,
    ): StateFlow<Boolean> =
        (recordings ?: flowOf(emptyList()))
            .combine(current) { list, channel -> isRecording(list, channel) }
            .stateIn(scope, SharingStarted.Eagerly, false)
}
