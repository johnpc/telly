package com.johncorser.telly.features.playback

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import com.johncorser.telly.features.player.PlayerScreenSurface

/**
 * Fullscreen playback host: video surface at the bottom of the stack, the
 * active overlay above it, D-pad keys routed through the ViewModel's
 * catalogue key map.
 */
@Composable
fun PlaybackScreen(deps: PlaybackDeps) {
    val scope = rememberCoroutineScope()
    val engine = remember { deps.engineFactory() }
    val viewModel =
        remember {
            PlaybackViewModel(
                env =
                    PlaybackEnv(
                        channelDao = deps.channelDao,
                        epgRepository = deps.epgRepository,
                        engine = engine,
                        store = deps.keyValueStore,
                        clock = deps.clock,
                    ),
                scope = scope,
            )
        }
    DisposableEffect(Unit) { onDispose { viewModel.close() } }
    val overlay by viewModel.overlay.collectAsState()
    BackHandler { viewModel.onKey(PlaybackKey.BACK) }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onPreviewKeyEvent { event -> onPreviewKey(event, overlay, viewModel) },
    ) {
        PlayerScreenSurface(engine, Modifier.fillMaxSize())
        if (overlay == PlaybackOverlay.None) {
            PlaybackScreenKeyAnchor(onKey = viewModel::onKey)
        }
        PlaybackScreenOverlays(viewModel, overlay)
    }
}

/** Invisible focus holder that turns raw key events into [PlaybackKey]s. */
@Composable
private fun PlaybackScreenKeyAnchor(onKey: (PlaybackKey) -> Boolean) {
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

/** MENU opens the context menu over any overlay; browsing keeps info alive. */
private fun onPreviewKey(
    event: KeyEvent,
    overlay: PlaybackOverlay,
    viewModel: PlaybackViewModel,
): Boolean {
    if (event.type != KeyEventType.KeyDown) return false
    viewModel.onOverlayInteraction()
    val wantsMenu = event.key == Key.Menu
    return if (wantsMenu && overlay != PlaybackOverlay.None) viewModel.onKey(PlaybackKey.MENU) else false
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
