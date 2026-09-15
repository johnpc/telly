package com.johncorser.telly.features.guide

/**
 * The nav rail's focusable stops, top to bottom: search, the live-TV
 * section icon (lit white — it IS the current section; OK returns focus to
 * the guide, mirroring RIGHT), and the settings gear. Pure so the vertical
 * chain stays JVM-testable; the composable wires FocusRequesters from it.
 */
enum class GuideRailStop {
    SEARCH,
    LIVE_TV,
    GEAR,
    ;

    /** The stop UP moves to, or null at the top. */
    fun above(): GuideRailStop? = entries.getOrNull(ordinal - 1)

    /** The stop DOWN moves to, or null at the bottom. */
    fun below(): GuideRailStop? = entries.getOrNull(ordinal + 1)
}
