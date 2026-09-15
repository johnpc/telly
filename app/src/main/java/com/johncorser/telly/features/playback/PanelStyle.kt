package com.johncorser.telly.features.playback

/**
 * Appearance -> Player, resolved to render values for the playback panels
 * (info-overlay scrims + the channel panel). "Panels transparency" is
 * EXTRA transparency over today's sampled alphas, so the 0% default keeps
 * telly's current rendering exactly; "Show clock" drives the top-scrim
 * clock (default on = today).
 */
data class PanelStyle(
    val alphaFactor: Float = 1f,
    val showClock: Boolean = true,
) {
    /** Today's alpha scaled by the chosen extra transparency. */
    fun scale(baseAlpha: Float): Float = baseAlpha * alphaFactor

    companion object {
        private const val PERCENT = 100f
        val DEFAULT = PanelStyle()

        fun from(
            extraTransparencyPercent: Int,
            showClock: Boolean,
        ): PanelStyle =
            PanelStyle(
                alphaFactor = 1f - extraTransparencyPercent / PERCENT,
                showClock = showClock,
            )
    }
}
