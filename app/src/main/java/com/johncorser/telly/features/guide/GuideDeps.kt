package com.johncorser.telly.features.guide

import com.johncorser.telly.features.playback.PlaybackDeps

/**
 * Everything the guide screen needs from the composition root: the
 * playback bundle (channels, EPG, engine factory, store, clock) plus the
 * settings the guide honors.
 */
class GuideDeps(
    val playback: PlaybackDeps,
    val pastDays: () -> Int,
)

/** Navigation the guide triggers: fullscreen playback, search, settings. */
class GuideCallbacks(
    val onFullscreen: () -> Unit,
    val onOpenSearch: () -> Unit,
    val onOpenSettings: () -> Unit,
)
