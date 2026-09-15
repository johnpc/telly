package com.johncorser.telly.features.guide

import com.johncorser.telly.features.playback.PlaybackDeps
import com.johncorser.telly.features.player.external.ExternalPlayer

/**
 * Everything the guide screen needs from the composition root: the
 * playback bundle (channels, EPG, engine factory, store, clock) plus the
 * settings the guide honors.
 */
class GuideDeps(
    val playback: PlaybackDeps,
    val pastDays: () -> Int,
)

/** Actions the guide triggers outside itself: navigation + external player. */
class GuideCallbacks(
    val onFullscreen: () -> Unit,
    val onOpenSearch: () -> Unit,
    val onOpenSettings: () -> Unit,
    val external: ExternalPlayer = ExternalPlayer.OFF,
)
