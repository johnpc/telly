package com.johncorser.telly.features.playback

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type

/** Invisible focus holder that turns raw key events into [PlaybackKey]s. */
@Composable
internal fun PlaybackScreenKeyAnchor(onKey: (PlaybackKey) -> Boolean) {
    val focusRequester = remember { FocusRequester() }
    val detector = remember { OkLongPressDetector() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    Box(
        Modifier
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event -> mapKeyEvent(event, detector)?.let(onKey) ?: false },
    )
}

/**
 * While the info overlay owns focus (cards / transport row), the keys the
 * catalogue maps to overlay transitions — UP to transport, long-OK / MENU to
 * quick-bar, CH± to zap — are intercepted before the focus system.
 */
internal fun onPreviewKey(
    event: KeyEvent,
    overlay: PlaybackOverlay,
    viewModel: PlaybackViewModel,
    detector: OkLongPressDetector,
): Boolean {
    if (event.type == KeyEventType.KeyDown) viewModel.onOverlayInteraction()
    if (overlay != PlaybackOverlay.Info && overlay != PlaybackOverlay.InfoTransport) return false
    return when (event.key) {
        Key.DirectionCenter, Key.Enter -> onPreviewCenter(event, viewModel, detector)
        Key.Menu, Key.DirectionUp, Key.ChannelUp, Key.ChannelDown ->
            event.type == KeyEventType.KeyDown && viewModel.onKey(previewKeyOf(event.key))
        else -> false
    }
}

/** Long-press OK fires LONG_OK once and swallows its release; short OK passes. */
private fun onPreviewCenter(
    event: KeyEvent,
    viewModel: PlaybackViewModel,
    detector: OkLongPressDetector,
): Boolean {
    if (event.type != KeyEventType.KeyDown) return detector.onUp() == null
    detector.onDown(event.nativeKeyEvent.repeatCount)?.let(viewModel::onKey)
    return event.nativeKeyEvent.repeatCount > 0
}

private fun previewKeyOf(key: Key): PlaybackKey =
    when (key) {
        Key.Menu -> PlaybackKey.MENU
        Key.DirectionUp -> PlaybackKey.UP
        Key.ChannelUp -> PlaybackKey.CHANNEL_UP
        else -> PlaybackKey.CHANNEL_DOWN
    }

private fun mapKeyEvent(
    event: KeyEvent,
    detector: OkLongPressDetector,
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
