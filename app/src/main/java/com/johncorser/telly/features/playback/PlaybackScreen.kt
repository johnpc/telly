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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import com.johncorser.telly.core.input.HoldKeyDetector
import com.johncorser.telly.core.ui.ScreenLifecycleStartStop
import com.johncorser.telly.features.panel.PanelLock
import com.johncorser.telly.features.pip.PipState
import com.johncorser.telly.features.player.PlayerScreenSurface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Fullscreen playback host: video surface at the bottom of the stack, the
 * active overlay above it, D-pad keys routed through the ViewModel's
 * catalogue key map.
 */
@Composable
fun PlaybackScreen(
    deps: PlaybackDeps,
    onExitToGuide: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onOpenSearch: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenMultiview: () -> Unit = {},
    onEnterPip: () -> Unit = {},
    pip: PipState = PipState.shared,
) {
    // A dedicated main-thread scope instead of rememberCoroutineScope(): the
    // ViewModel drives ExoPlayer (main-thread-affine) and wall-clock overlay
    // timeouts, so its coroutines must not run on the composition's frame
    // clock (under UI-test harnesses that clock defers/redirects resumes).
    val scope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
    val engine = remember { deps.engineFactory() }
    val viewModel =
        remember {
            PlaybackViewModel(
                env =
                    PlaybackEnv(
                        channelDao = deps.sources.channelDao,
                        epgRepository = deps.sources.epgRepository,
                        engine = engine,
                        store = deps.keyValueStore,
                        time = PlaybackTime(deps.clock),
                        hooks =
                            PlaybackHooks(
                                panelLock = PanelLock(deps.parental),
                                onOpenSettings = onOpenSettings,
                                onOpenMultiview = onOpenMultiview,
                                onEnterPip = onEnterPip,
                                pip = pip,
                            ),
                    ),
                history = deps.sources.history,
                scope = scope,
                onExitToGuide = onExitToGuide,
                onOpenHistory = onOpenHistory,
                openSearch = onOpenSearch,
            )
        }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.close()
            scope.cancel()
        }
    }
    val overlay by viewModel.overlay.collectAsState()
    val inPip by pip.inPip.collectAsState()
    val previewDetector = remember { HoldKeyDetector(PlaybackKey.OK, PlaybackKey.LONG_OK) }
    // Background/resume: stop the stream on STOP, re-tune on the START
    // after it — the reference re-tunes on resume (round7 resume P2).
    ScreenLifecycleStartStop(onStart = viewModel.lifecycle::onForeground, onStop = viewModel.lifecycle::onBackground)
    BackHandler { viewModel.onKey(PlaybackKey.BACK) }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onPreviewKeyEvent { event -> onPreviewKey(event, overlay, viewModel, previewDetector) },
    ) {
        PlayerScreenSurface(engine, Modifier.fillMaxSize())
        // In the tiny PIP window every piece of chrome hides: clean video only.
        if (!inPip) {
            if (overlay == PlaybackOverlay.None || overlay == PlaybackOverlay.ZapInfo) {
                PlaybackScreenKeyAnchor(onKey = viewModel::onKey)
            }
            PlaybackScreenOverlays(viewModel, overlay)
        }
    }
}
