package com.johncorser.telly.core.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onKeyEvent

/**
 * Invisible focus holder owning the D-pad for screens that route raw key
 * events through a pure key map (fullscreen playback, the guide grid).
 */
@Composable
fun TellyScreenKeyAnchor(onEvent: (KeyEvent) -> Boolean) {
    Box(
        Modifier
            .focusOnAppear()
            .focusable()
            .onKeyEvent(onEvent),
    )
}
