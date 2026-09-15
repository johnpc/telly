package com.johncorser.telly.features.vod

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.johncorser.telly.features.player.PlayerScreenSurface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Fullscreen VOD playback with a seek transport (ux-spec §VOD): the screen
 * owns its engine like Multiview does, the invisible key anchor routes the
 * D-pad through the ViewModel, and BACK persists the resume position before
 * popping. A stored mid-watch position swaps in the Resume/Start-over step.
 */
@Composable
fun VodPlaybackScreen(
    deps: VodDeps,
    itemKey: String,
    onExit: () -> Unit,
) {
    // Dedicated main scope: the ViewModel drives an ExoPlayer, which is
    // main-thread-affine (the PlaybackScreen/Multiview pattern).
    val scope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
    val engine = remember { deps.engineFactory() }
    val viewModel = remember { VodPlaybackViewModel(deps, itemKey, engine, scope, onExit) }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.release()
            scope.cancel()
        }
    }
    LaunchedEffect(Unit) { viewModel.start() }
    BackHandler { viewModel.exit() }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        PlayerScreenSurface(engine, Modifier.fillMaxSize())
        VodPlaybackScreenLayers(viewModel)
    }
}
