package com.johncorser.telly.features.player.afr

import kotlin.math.abs

/**
 * Pure AFR mode choice: given the display's modes and the content frame
 * rate, pick the mode at the CURRENT resolution whose refresh rate is an
 * integer multiple of the content rate. NTSC fractional rates fuzzy-match
 * their integer siblings (23.976/24, 29.97/30, 59.94/60) via a 0.2 % ratio
 * tolerance — the 1000/1001 wobble is ~0.1 %. Smaller multiples win, so on
 * a typical TV mode set this yields 24 Hz for 23.976/24 fps film, 50 Hz
 * for 25 fps PAL (no 25 Hz mode exists) and 59.94/60 Hz for 29.97/30 fps,
 * with 59.94 preferred over 60 for NTSC content by the closer ratio.
 */
object DisplayModeMatcher {
    private const val RATIO_TOLERANCE = 0.002f
    private const val MAX_MULTIPLE = 8

    /**
     * The mode id to switch to, or null when nothing fits — including when
     * the current mode already matches, so callers never switch needlessly.
     */
    fun bestModeId(
        modes: AfrModeSet,
        contentFps: Float,
    ): Int? {
        if (contentFps <= 0f || multipleOf(modes.current.refreshRate, contentFps) != null) return null
        return modes.all
            .filter { it.width == modes.current.width && it.height == modes.current.height }
            .mapNotNull { mode -> candidateOf(mode, contentFps) }
            .minWithOrNull(compareBy({ it.multiple }, { it.ratioError }))
            ?.mode
            ?.id
    }

    /** The k where refresh ≈ k × fps within the NTSC-tolerant ratio, or null. */
    fun multipleOf(
        refreshRate: Float,
        contentFps: Float,
    ): Int? = (1..MAX_MULTIPLE).firstOrNull { k -> ratioError(refreshRate, contentFps, k) <= RATIO_TOLERANCE }

    private fun ratioError(
        refreshRate: Float,
        contentFps: Float,
        multiple: Int,
    ): Float = abs(refreshRate / (multiple * contentFps) - 1f)

    private fun candidateOf(
        mode: AfrMode,
        contentFps: Float,
    ): Candidate? =
        multipleOf(mode.refreshRate, contentFps)
            ?.let { k -> Candidate(mode, k, ratioError(mode.refreshRate, contentFps, k)) }

    private data class Candidate(
        val mode: AfrMode,
        val multiple: Int,
        val ratioError: Float,
    )
}
