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
    /** "Confirm exit by second press Back" (default off = exit immediately). */
    val confirmExit: () -> Boolean = { false },
    /** False exactly once after a cold start with "last channel on start" off. */
    val resumePreview: () -> Boolean = { true },
)

/** Navigation the guide triggers: fullscreen playback, search, settings. */
class GuideCallbacks(
    val onFullscreen: () -> Unit,
    val onOpenSearch: () -> Unit,
    val onOpenSettings: () -> Unit,
)
