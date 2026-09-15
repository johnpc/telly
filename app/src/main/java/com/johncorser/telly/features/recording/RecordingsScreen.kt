package com.johncorser.telly.features.recording

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * The Recordings / DVR library (Route.Recordings): reached from the
 * quick-bar's Recordings slot and the guide rail's DVR icon. Rows list
 * title / channel / start / status; OK plays DONE captures fullscreen,
 * stops or cancels the rest (GuidedStep confirm), long-OK deletes. Thin —
 * layout lives in [RecordingsScreenContent], state in the view model.
 */
@Composable
fun RecordingsScreen(deps: RecordingDeps) {
    val scope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
    val viewModel = remember { RecordingsViewModel(deps.center, deps.zone, scope) }
    DisposableEffect(Unit) { onDispose { scope.cancel() } }
    val rows by viewModel.rows.collectAsState()
    val confirm by viewModel.confirm.collectAsState()
    val playing by viewModel.playing.collectAsState()
    // BACK closes the confirm before it can pop the whole route.
    BackHandler(enabled = confirm != null) { viewModel.dismissConfirm() }
    playing?.let { row ->
        RecordingScreenPlayer(deps, row, onExit = viewModel::stopPlaying)
        return
    }
    RecordingsScreenContent(
        rows = rows,
        onClick = viewModel::onRowClick,
        onLongClick = viewModel::onRowLongClick,
    )
    confirm?.let {
        RecordingScreenLibraryConfirm(
            confirm = it,
            onAccept = viewModel::onConfirmAccepted,
            onDismiss = viewModel::dismissConfirm,
        )
    }
}
