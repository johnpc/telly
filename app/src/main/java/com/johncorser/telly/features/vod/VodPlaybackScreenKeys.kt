package com.johncorser.telly.features.vod

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

/**
 * VOD transport key map (ux-spec §VOD + §3.6): OK pauses, LEFT/RIGHT seek
 * by the skip step, RW/FF jump further, UP/DOWN just reveal the transport.
 * BACK stays unhandled here so the screen's BackHandler persists and pops.
 */
internal fun onVodPlaybackKey(
    event: KeyEvent,
    controls: VodPlayerControls,
): Boolean {
    if (event.type != KeyEventType.KeyDown) return false
    return when (event.key) {
        Key.DirectionCenter, Key.Enter -> {
            controls.togglePause()
            true
        }
        Key.DirectionLeft -> {
            controls.seekBy(-VodPlaybackViewModel.SEEK_STEP_MS)
            true
        }
        Key.DirectionRight -> {
            controls.seekBy(VodPlaybackViewModel.SEEK_STEP_MS)
            true
        }
        Key.MediaRewind -> {
            controls.seekBy(-VodPlaybackViewModel.JUMP_STEP_MS)
            true
        }
        Key.MediaFastForward -> {
            controls.seekBy(VodPlaybackViewModel.JUMP_STEP_MS)
            true
        }
        Key.DirectionUp, Key.DirectionDown -> {
            controls.transport.poke(controls.paused.value)
            true
        }
        else -> false
    }
}
