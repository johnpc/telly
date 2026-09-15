package com.johncorser.telly.testutil

import com.johncorser.telly.features.player.tracks.TrackFacade
import com.johncorser.telly.features.player.tracks.TrackSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** In-memory [TrackFacade] recording selections, for plain-JVM picker tests. */
class FakeTrackFacade(
    initial: TrackSnapshot = TrackSnapshot(),
) : TrackFacade {
    override val snapshot = MutableStateFlow(initial)
    private val offset = MutableStateFlow(0L)
    override val audioOffsetMs: StateFlow<Long> = offset

    val videoSelections = mutableListOf<String?>()
    val audioSelections = mutableListOf<String>()
    val textSelections = mutableListOf<String?>()

    override fun selectVideo(id: String?) {
        videoSelections += id
        snapshot.value = snapshot.value.copy(videoOverrideId = id)
    }

    override fun selectAudio(id: String) {
        audioSelections += id
        snapshot.value = snapshot.value.copy(selectedAudioId = id)
    }

    override fun selectText(id: String?) {
        textSelections += id
        snapshot.value = snapshot.value.copy(selectedTextId = id)
    }

    override fun setAudioOffsetMs(ms: Long) {
        offset.value = ms
    }
}
