package com.johncorser.telly.core.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * TiviMate's GuidedStep transition, measured frame-by-frame on the reference
 * app (settings-root -> guided step, both directions): a fast ~120 ms
 * cross-fade with the incoming content already at its final position — no
 * slide. Used between wizard steps and top-level routes.
 */
@Composable
fun <T> ScreenCrossfade(
    target: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    Crossfade(
        targetState = target,
        modifier = modifier,
        animationSpec = tween(durationMillis = 120, easing = LinearEasing),
        label = "screen-crossfade",
        content = content,
    )
}
