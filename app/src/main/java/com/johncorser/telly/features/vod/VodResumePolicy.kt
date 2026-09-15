package com.johncorser.telly.features.vod

/**
 * When a stored position is worth resuming (ux-spec §VOD): between 5% and
 * 95% of the duration telly offers Resume/Start-over; past 95% the item
 * counts as finished and its position is cleared.
 */
object VodResumePolicy {
    private const val PERMILLE = 1000L
    private const val MIN_RESUME_PERMILLE = 50L
    private const val MAX_RESUME_PERMILLE = 950L

    fun offerResume(
        positionMs: Long,
        durationMs: Long,
    ): Boolean = permilleOf(positionMs, durationMs) in MIN_RESUME_PERMILLE..MAX_RESUME_PERMILLE

    fun finished(
        positionMs: Long,
        durationMs: Long,
    ): Boolean = permilleOf(positionMs, durationMs) > MAX_RESUME_PERMILLE

    private fun permilleOf(
        positionMs: Long,
        durationMs: Long,
    ): Long = if (durationMs <= 0) -1 else positionMs * PERMILLE / durationMs
}
