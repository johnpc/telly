package com.johncorser.telly.features.playback

import com.johncorser.telly.features.panel.PanelLock
import com.johncorser.telly.features.player.external.ExternalPlayer

/**
 * Cross-slice hooks the playback surface plugs into (nav + parental +
 * platform glue). MainActivity supplies the platform-facing callbacks
 * (AFR display-mode switching, the external-player chooser); the playback
 * screen copies its own navigation lambdas in on top.
 */
data class PlaybackHooks(
    val panelLock: PanelLock = PanelLock(),
    val onOpenSettings: () -> Unit = {},
    val onOpenMultiview: () -> Unit = {},
    /** AFR: the engine detected (or render-estimated) a content frame rate. */
    val onFrameRateChanged: (Float) -> Unit = {},
    /** AFR restore point: fullscreen playback left composition. */
    val onPlaybackStopped: () -> Unit = {},
    /** External player policy + chooser launcher (features/player/external). */
    val external: ExternalPlayer = ExternalPlayer.OFF,
)
