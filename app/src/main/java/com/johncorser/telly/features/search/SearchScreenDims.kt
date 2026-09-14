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

    /** History block aligns with the query bar (orb + gap), uidump 49. */
    val historyStart = orbStart + orbSize + barGap
    val headerTop = 32.dp
    val shelfTop = 14.dp

    val cardWidth = 124.dp
    val cardGap = 8.dp
    val cardPad = 12.dp
    val logoWidth = 100.dp
    val logoHeight = 42.dp

    val rowHeight = 72.dp
    val listWidth = 580.dp
    val rowCardWidth = 120.dp
    val rowLogoWidth = 72.dp
    val rowLogoHeight = 48.dp
    val rowTextStart = 36.dp

    val detailWidth = 332.dp
    val detailTop = 40.dp
    val detailPad = 16.dp

    val barFill = Color(0xFFB8BCC0)
    val barHint = Color(0xFF75797D)
    val barText = Color(0xFF202124)
    val cardFill = Color(0xFF1B222B)
    val detailFill = Color(0xFF212830)
}
