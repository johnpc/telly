package com.johncorser.telly.features.player

import kotlinx.coroutines.flow.StateFlow

/** Playback status exposed to the UI; [Error] carries a displayable cause. */
sealed interface PlayerState {
    data object Idle : PlayerState

    data object Buffering : PlayerState

    data object Playing : PlayerState

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

    fun load(streamUrl: String)

    fun stop()

    fun release()
}
