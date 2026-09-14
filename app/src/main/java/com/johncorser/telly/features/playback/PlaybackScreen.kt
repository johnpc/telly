package com.johncorser.telly.features.playback

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import com.johncorser.telly.core.input.HoldKeyDetector
import com.johncorser.telly.features.player.PlayerScreenSurface

/**
 * Fullscreen playback host: video surface at the bottom of the stack, the
 * active overlay above it, D-pad keys routed through the ViewModel's
 * catalogue key map.
 */
@Composable
fun PlaybackScreen(
    deps: PlaybackDeps,
    onExitToGuide: () -> Unit = {},
) {
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
                onExitToGuide = onExitToGuide,
            )
        }
    DisposableEffect(Unit) { onDispose { viewModel.close() } }
    val overlay by viewModel.overlay.collectAsState()
    val previewDetector = remember { HoldKeyDetector(PlaybackKey.OK, PlaybackKey.LONG_OK) }
    BackHandler { viewModel.onKey(PlaybackKey.BACK) }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onPreviewKeyEvent { event -> onPreviewKey(event, overlay, viewModel, previewDetector) },
    ) {
        PlayerScreenSurface(engine, Modifier.fillMaxSize())
        if (overlay == PlaybackOverlay.None || overlay == PlaybackOverlay.ZapInfo) {
            PlaybackScreenKeyAnchor(onKey = viewModel::onKey)
        }
        PlaybackScreenOverlays(viewModel, overlay)
    }
}
