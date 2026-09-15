package com.johncorser.telly.core.design

/**
 * Appearance -> Font size, applied as a theme-level fontScale multiplier
 * over the device density. "Medium" (the captured TiviMate default) is
 * exactly 1.0 = today's rendering.
 */
object FontScale {
    private const val SMALL = 0.85f
    private const val LARGE = 1.15f
    private const val HUGE = 1.3f

    fun factorFor(label: String): Float =
        when (label) {
            "Small" -> SMALL
            "Large" -> LARGE
            "Huge" -> HUGE
            else -> 1f
        }
}
