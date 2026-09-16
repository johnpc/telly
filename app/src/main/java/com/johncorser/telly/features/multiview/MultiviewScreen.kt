package com.johncorser.telly.features.multiview

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED
import com.johncorser.telly.features.playback.PlaybackScreenBlockGate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Multiview host (multiview-round captures): the pane grid on black, the
 * teaser hint under a single pane, and the menu/picker layers above. Thin —
 * all transitions live in [MultiviewViewModel].
 */
@Composable
fun MultiviewScreen(
    deps: MultiviewDeps,
    onExit: () -> Unit = {},
) {
    // Same dedicated main scope as PlaybackScreen: the ViewModel drives
    // ExoPlayers, which are main-thread-affine.
    val scope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
    val viewModel = remember { MultiviewViewModel(deps, scope, onExit) }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.close()
            scope.cancel()
        }
    }
    LaunchedEffect(Unit) { viewModel.start() }
    val panes by viewModel.panes.panes.collectAsState()
    val focusedId by viewModel.panes.focusedId.collectAsState()
    val layer by viewModel.layer.collectAsState()
    val pinChannel by viewModel.gate.pinPrompt.collectAsState()
    BackHandler { viewModel.onBack() }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onPreviewKeyEvent { event -> onChannelZapKey(event, viewModel) },
    ) {
        MultiviewScreenGrid(
            viewModel,
            panes,
            focusedId,
            panesFocusable = layer == MultiviewLayer.Panes && pinChannel == null,
        )
        if (panes.size == 1) MultiviewScreenHint(Modifier.align(Alignment.BottomCenter))
        when (layer) {
            MultiviewLayer.Panes -> Unit
            MultiviewLayer.Menu -> MultiviewScreenMenu(viewModel, panes, focusedId)
            is MultiviewLayer.Picker -> MultiviewScreenPicker(viewModel)
        }
        // The blocked-channel tune gate, over every multiview layer: the
        // shared centered PIN card; wrong or cancelled PINs never tune.
        if (pinChannel != null) {
            PlaybackScreenBlockGate(
                onSubmit = viewModel.gate::submit,
                onDismiss = viewModel::onBack,
                keyboard = viewModel.gate.keyboardPin,
            )
        }
    }
}

/** The teaser hint under the single pane, copy verbatim (multiview-round 03). */
@Composable
private fun MultiviewScreenHint(modifier: Modifier) {
    Column(modifier.padding(bottom = 51.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Press OK to show menu", color = Color.White, fontSize = 17.sp)
        Text(
            text = "Your IPTV provider may limit the number of concurrent connections",
            color = Color(TELLY_TEXT_MUTED),
            fontSize = 15.sp,
        )
    }
}
