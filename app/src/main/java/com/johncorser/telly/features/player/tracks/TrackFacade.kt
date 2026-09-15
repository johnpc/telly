package com.johncorser.telly.features.player.tracks

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Track-selection seam of the playback engine (quick-bar pickers, catalogue
 * §3 stream slots): logic talks to this interface only, so JVM tests inject
 * a fake and never touch ExoPlayer's track APIs.
 */
interface TrackFacade {
    val snapshot: StateFlow<TrackSnapshot>

    /** Audio-sync offset, positive = audio delayed. Per-session, not persisted. */
    val audioOffsetMs: StateFlow<Long>

    /** Applies a video override; null returns to adaptive "Auto". */
    fun selectVideo(id: String?)

    fun selectAudio(id: String)

    /** Enables the given text track; null turns captions off. */
    fun selectText(id: String?)

    fun setAudioOffsetMs(ms: Long)

    companion object {
        /** Inert facade for engines without track support (fakes, tests). */
        val NONE: TrackFacade = NoTracks
    }
}

private object NoTracks : TrackFacade {
    override val snapshot: StateFlow<TrackSnapshot> = MutableStateFlow(TrackSnapshot())
    override val audioOffsetMs: StateFlow<Long> = MutableStateFlow(0L)

    override fun selectVideo(id: String?) = Unit

    override fun selectAudio(id: String) = Unit

    override fun selectText(id: String?) = Unit

    override fun setAudioOffsetMs(ms: Long) = Unit
}
