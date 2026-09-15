package com.johncorser.telly.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester

/**
 * A [FocusRequester] that grabs focus when its composable first appears —
 * the "first row/icon takes focus" pattern every TiviMate surface follows.
 */
@Composable
fun rememberAutoFocus(): FocusRequester {
    val requester = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { requester.requestFocus() } }
    return requester
}

/**
 * Re-requests focus once per frame until [landed] reports the grab stuck
 * or [maxFrames] frames elapsed (~500 ms at 60 fps: covers the settings
 * pane cross-fade, 300 ms, with margin). While an `AnimatedContent` pane
 * switch is mid-transition the focus system silently denies moving focus
 * into the entering content, and a single-shot request lets focus fall
 * through to whatever focusable lies under the sheet once the outgoing
 * row unmounts (device-verified on tv34: the grab is denied until the
 * exiting pane leaves, ~frame 20). Request failures (node not yet
 * attached) are swallowed and retried.
 */
suspend fun grabFocusUntilLanded(
    landed: () -> Boolean,
    request: () -> Unit,
    awaitFrame: suspend () -> Unit,
    maxFrames: Int = FOCUS_GRAB_FRAMES,
) {
    var frame = 0
    while (!landed() && frame < maxFrames) {
        runCatching { request() }
        awaitFrame()
        frame++
    }
}

private const val FOCUS_GRAB_FRAMES = 30
