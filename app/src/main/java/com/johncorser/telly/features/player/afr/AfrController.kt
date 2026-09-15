package com.johncorser.telly.features.player.afr

/**
 * AFR orchestration (fed by PlayerPlatformHooks.onFrameRateChanged): when the
 * setting is on and the content frame rate is known, switch the display to
 * the [DisplayModeMatcher]'s best mode at the current resolution. The
 * FIRST switch remembers the original mode; playback stop / app background
 * restores it only on the "also switch refresh rate on stop" variant.
 */
class AfrController(
    private val preference: () -> AfrPreference,
    private val display: AfrDisplay,
) {
    private var originalModeId: Int? = null

    fun onFrameRate(contentFps: Float) {
        if (!preference().enabled) return
        val modes = display.modes() ?: return
        val target = DisplayModeMatcher.bestModeId(modes, contentFps) ?: return
        if (originalModeId == null) originalModeId = modes.current.id
        display.apply(target)
    }

    /** Playback stopped or the app backgrounded (MainActivity onStop). */
    fun onPlaybackStopped() {
        val original = originalModeId ?: return
        originalModeId = null
        if (preference().restoreOnStop) display.apply(original)
    }
}
