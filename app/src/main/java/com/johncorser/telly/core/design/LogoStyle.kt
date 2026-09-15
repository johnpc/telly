package com.johncorser.telly.core.design

/**
 * Appearance -> Logos, resolved to the channel-logo tile treatment used
 * everywhere a logo renders (guide rows, panel, overlays, search,
 * history). The default is telly's current tile: the sampled #2C5F8A
 * fill behind the logo with 4 dp rounded corners.
 */
data class LogoStyle(
    val background: Long = TELLY_LOGO_TILE,
    val cornerDp: Float = DEFAULT_CORNER_DP,
) {
    companion object {
        const val DEFAULT_CORNER_DP = 4f
        const val TRANSPARENT = 0x00000000L
        const val DARK = 0xFF262D38L
        const val LIGHT = 0xFFE0E2E4L
        val DEFAULT = LogoStyle()

        /** "Logo background" label -> tile fill (unknown labels = default). */
        fun backgroundFor(label: String): Long =
            when (label) {
                "Transparent" -> TRANSPARENT
                "Dark" -> DARK
                "Light" -> LIGHT
                else -> TELLY_LOGO_TILE
            }

        fun from(
            background: String,
            roundedCorners: Boolean,
        ): LogoStyle =
            LogoStyle(
                background = backgroundFor(background),
                cornerDp = if (roundedCorners) DEFAULT_CORNER_DP else 0f,
            )
    }
}
