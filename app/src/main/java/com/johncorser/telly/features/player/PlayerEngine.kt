package com.johncorser.telly.features.player

import com.johncorser.telly.features.player.tracks.TrackFacade
import kotlinx.coroutines.flow.StateFlow

/** Playback status exposed to the UI; [Error] carries a displayable cause. */
sealed interface PlayerState {
    data object Idle : PlayerState

    data object Buffering : PlayerState

    data object Playing : PlayerState

    /** A finite stream (archive / VOD / capture) reached its end. */
    data object Ended : PlayerState

    data class Error(
        val message: String,
    ) : PlayerState
}

/** Stream characteristics feeding the info-overlay badges (HD / FPS / MONO). */
data class VideoDetails(
    val width: Int,
    val height: Int,
    val frameRate: Float,
    val audioChannels: Int,
)

/**
 * Seam between playback logic and Media3 (see CLAUDE.md decisions log):
 * ViewModels talk to this interface only, so JVM tests inject a fake engine
 * and never touch ExoPlayer.
 */
interface PlayerEngine {
    val state: StateFlow<PlayerState>
    val video: StateFlow<VideoDetails?>

    /** True while playback is user-paused; [load] and [stop] reset it. */
    val paused: StateFlow<Boolean>

    fun load(streamUrl: String)

    fun stop()

    fun release()

    /** Multiview audio ownership: only the focused pane's engine is unmuted. */
    fun setMuted(muted: Boolean)

    /** User pause (catch-up transport ⏸); a no-op while already paused. */
    fun pause()

    /** Resumes a [pause]d stream. */
    fun resume()

    /** Current playback position (catch-up transport readout). */
    fun positionMs(): Long

    /** Absolute seek within a finite (catch-up) stream. */
    fun seekTo(positionMs: Long)

    /** Track selection + audio-sync seam (quick-bar pickers); inert by default. */
    val tracks: TrackFacade get() = TrackFacade.NONE

    /** Live Hardware/Software decoder preference; tunes apply per-channel overrides. */
    val decoders: DecoderPreferences get() = DecoderPreferences.NONE
}
