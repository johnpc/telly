package com.johncorser.telly.features.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.johncorser.telly.R
import com.johncorser.telly.core.design.TELLY_ONBOARDING_BACKGROUND
import com.johncorser.telly.core.ui.TellyScreenMutedText
import com.johncorser.telly.features.playback.PlaybackDeps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.util.TimeZone

/**
 * The full-screen History list (history-round2 §3): title "History" +
 * clear-all trash top-RIGHT, rows of recently watched channels (or a
 * centered "No history"), BACK popping to the fullscreen player.
 * Deviation, on purpose: the reference dims the live video behind; telly
 * has a single player engine per route, so it sits on the app background
 * (the Search precedent).
 */
@Composable
fun HistoryScreen(
    deps: PlaybackDeps,
    onTuned: () -> Unit,
) {
    val scope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
    val viewModel =
        remember { HistoryViewModel(deps.sources, deps.keyValueStore, TimeZone.getDefault(), scope) }
    DisposableEffect(Unit) { onDispose { scope.cancel() } }
    val rows by viewModel.rows.collectAsState()
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(TELLY_ONBOARDING_BACKGROUND)),
    ) {
        HistoryScreenHeader(viewModel, focusTrash = rows.isEmpty())
        if (rows.isEmpty()) {
            Box(Modifier.align(Alignment.Center)) {
                TellyScreenMutedText(text = stringResource(R.string.history_empty), fontSize = 18.sp)
            }
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(start = 40.dp, top = 80.dp, end = 40.dp),
            ) {
                items(rows, key = { it.channel.id }) { row ->
                    HistoryScreenRow(
                        row = row,
                        requestFocus = row == rows.first(),
                        onClick = {
                            viewModel.tune(row)
                            onTuned()
                        },
                    )
                }
            }
        }
    }
}
