package com.johncorser.telly.features.search

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Search-screen geometry from the 1920x1080 uidumps 49/50 (2 px = 1 dp) and
 * colors sampled from the captures. The reference dims the live video behind
 * the screen; telly's search is a route of its own, so it sits on the flat
 * app background instead (single-player-engine decision).
 */
internal object SearchScreenDims {
    val orbSize = 52.dp
    val orbStart = 56.dp
    val topBarTop = 26.dp
    val barHeight = 56.dp
    val barWidth = 600.dp
    val barGap = 70.dp
    val barCorner = 8.dp
    val barTextPad = 24.dp
    val gearSize = 40.dp
    val edgePad = 24.dp

    /** History header starts at x=384 px like the live bar column (tm-01). */
    val historyStart = 192.dp

    /**
     * Resting search results render at 42% opacity — every tm-01/02/03
     * sample (headers, titles, times, airing blue, logos, card fills) is
     * its full color x 0.42 composited over the flat background.
     */
    const val RESTING_ALPHA = 0.42f
    val headerTop = 32.dp
    val shelfTop = 14.dp

    val cardWidth = 124.dp
    val cardGap = 8.dp

    /** Card text starts 16 px in from the card edge — x=64 px on screen (tm-02). */
    val cardPad = 8.dp
    val logoWidth = 100.dp
    val logoHeight = 42.dp

    /** Airing rows pitch 119-120 px in the two-pane Programs section (round6 06/08). */
    val rowHeight = 60.dp

    /** Row title boxes are a fixed 776 px wide plus the pill's 2 x 8 dp pads (round6 06). */
    val rowsWidth = 404.dp
    val rowCardWidth = 120.dp
    val rowLogoWidth = 72.dp
    val rowLogoHeight = 48.dp

    /** Master-lane channel cards: 240x206 px, adjacent, logo inset 32 px (round6 08). */
    val masterCardHeight = 103.dp
    val masterCardPad = 16.dp
    val masterNameGap = 4.dp

    /** Plus the pill's 8 dp text pad this puts row titles at x=360 px (tm-02, round6 06). */
    val rowTextStart = 28.dp

    val detailWidth = 332.dp
    val detailTop = 40.dp
    val detailPad = 16.dp

    val barFill = Color(0xFFB8BCC0)
    val barHint = Color(0xFF75797D)
    val barText = Color(0xFF202124)
    val cardFill = Color(0xFF1B222B)
    val detailFill = Color(0xFF212830)
}
