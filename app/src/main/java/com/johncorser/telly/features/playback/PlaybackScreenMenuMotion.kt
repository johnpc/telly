package com.johncorser.telly.features.playback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

// Motion of the shared long-OK context sheet, frame-measured in ref-round6
// §A (01-sheet-open-close-take1-best.mp4 + 01-frames/): the reference
// entrance is a short DECELERATING fade with a small slide from the right —
// the single emitted mid-frame at ~140 ms is already at ~78-80% opacity and
// 9-10 px right of its settled x. 250 ms of FastOutSlowIn is ~79% through
// at 140 ms and lands inside the observed 150-280 ms envelope; the slide's
// total travel is the 10 px (= 5 dp at 2 px/dp) mid-frame displacement —
// the largest ever emitted, so the whole animation stays in that class.
// The sheet's EXIT is an instant cut (zero intermediate frames in any
// take): closing simply uncomposes it. Only the backdrop scrim fades back
// out, ~300 ms (scrim gone 5.904 → 6.203 s).

/** Entrance duration; the scrim fade-in matches so both settle together. */
internal const val MENU_SHEET_ENTER_MS = 250

/** Scrim fade-out after the sheet's instant cut (ref-round6 §A close). */
private const val MENU_SCRIM_EXIT_MS = 300

/** Slide-in distance: the reference mid-frame's 9-10 px = 5 dp. */
private val MENU_SHEET_SLIDE = 5.dp

// The grid behind the open sheet dims (15,20,22) → (4,6,6) ≈ 72% black.
private const val MENU_SCRIM = 0xB8000000

/** Decelerating fade + 5 dp slide from the right (ref-round6 §A open). */
@Composable
internal fun playerMenuSheetEnter(): EnterTransition {
    val slide = with(LocalDensity.current) { MENU_SHEET_SLIDE.roundToPx() }
    return fadeIn(tween(MENU_SHEET_ENTER_MS, easing = FastOutSlowInEasing)) +
        slideInHorizontally(tween(MENU_SHEET_ENTER_MS, easing = FastOutSlowInEasing)) { slide }
}

/**
 * Backdrop dim behind the sheet: settles within a frame of the sheet on
 * open, then outlives the sheet's instant cut with a ~300 ms fade-out —
 * hosts keep it composed across the close, so it must sit OUTSIDE the
 * sheet's own layer switch.
 */
@Composable
internal fun PlaybackScreenMenuScrim(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(MENU_SHEET_ENTER_MS, easing = FastOutSlowInEasing)),
        exit = fadeOut(tween(MENU_SCRIM_EXIT_MS)),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(MENU_SCRIM)),
        )
    }
}
