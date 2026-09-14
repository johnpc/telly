package com.johncorser.telly.features.playback

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import com.johncorser.telly.core.input.HoldKeyDetector

/** MENU opens the context menu over any overlay; browsing keeps info alive. */
internal fun onPreviewKey(
    event: KeyEvent,
    overlay: PlaybackOverlay,
    viewModel: PlaybackViewModel,
): Boolean {
    if (event.type != KeyEventType.KeyDown) return false
    viewModel.onOverlayInteraction()
    val wantsMenu = event.key == Key.Menu
    return if (wantsMenu && overlay != PlaybackOverlay.None) viewModel.onKey(PlaybackKey.MENU) else false
}

/** Raw KeyEvents → [PlaybackKey]s, with OK/long-OK split by the detector. */
internal fun mapKeyEvent(
    event: KeyEvent,
    detector: HoldKeyDetector<PlaybackKey>,
): PlaybackKey? {
    val down = event.type == KeyEventType.KeyDown
    return when (event.key) {
        Key.DirectionCenter, Key.Enter ->
            if (down) detector.onDown(event.nativeKeyEvent.repeatCount) else detector.onUp()
        Key.DirectionUp -> PlaybackKey.UP.takeIf { down }
        Key.DirectionDown -> PlaybackKey.DOWN.takeIf { down }
        Key.DirectionLeft -> PlaybackKey.LEFT.takeIf { down }
        Key.DirectionRight -> PlaybackKey.RIGHT.takeIf { down }
        Key.ChannelUp -> PlaybackKey.CHANNEL_UP.takeIf { down }
        Key.ChannelDown -> PlaybackKey.CHANNEL_DOWN.takeIf { down }
        Key.Menu -> PlaybackKey.MENU.takeIf { down }
        else -> null
    }
}
