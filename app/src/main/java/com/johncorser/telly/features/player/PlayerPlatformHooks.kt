package com.johncorser.telly.features.player

import com.johncorser.telly.features.player.external.ExternalPlayer

/**
 * MainActivity's platform-facing playback glue, bundled (the MyListHooks
 * parameter-object precedent) so PlaybackHooks stays a short parameter
 * list: AFR display-mode switching plus the external-player chooser.
 */
class PlayerPlatformHooks(
    /** AFR: the engine detected (or render-estimated) a content frame rate. */
    val onFrameRateChanged: (Float) -> Unit = {},
    /** AFR restore point: fullscreen playback left composition. */
    val onPlaybackStopped: () -> Unit = {},
    /** External player policy + chooser launcher (features/player/external). */
    val external: ExternalPlayer = ExternalPlayer.OFF,
)
