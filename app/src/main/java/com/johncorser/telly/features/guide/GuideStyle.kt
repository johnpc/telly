package com.johncorser.telly.features.guide

/**
 * Appearance -> TV guide, resolved to render values. The defaults are
 * telly's current pixel-parity rendering: 7 visible channels = the 39 dp
 * row pitch (uidump 24), an opaque #131619 background, numbers shown.
 */
data class GuideStyle(
    val rowHeightDp: Float = GuideGeometry.ROW_HEIGHT_DP,
    val backgroundAlpha: Float = OPAQUE,
    val showChannelNumbers: Boolean = true,
) {
    companion object {
        const val OPAQUE = 1f
        private const val ALPHA_90 = 0.9f
        private const val ALPHA_80 = 0.8f
        private const val ALPHA_70 = 0.7f
        val DEFAULT = GuideStyle()

        /** Row pitch scaled so [visibleChannels] rows fill today's 7-row band. */
        fun rowHeightFor(visibleChannels: Int): Float =
            GuideGeometry.ROW_HEIGHT_DP * GuideGeometry.VISIBLE_ROWS / visibleChannels

        /** "Panel transparency" labels -> guide background alpha. */
        fun backgroundAlphaFor(label: String): Float =
            when (label) {
                "90%" -> ALPHA_90
                "80%" -> ALPHA_80
                "70%" -> ALPHA_70
                else -> OPAQUE
            }

        fun from(
            visibleChannels: Int,
            transparency: String,
            showChannelNumbers: Boolean,
        ): GuideStyle =
            GuideStyle(
                rowHeightDp = rowHeightFor(visibleChannels),
                backgroundAlpha = backgroundAlphaFor(transparency),
                showChannelNumbers = showChannelNumbers,
            )
    }
}
