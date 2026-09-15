package com.johncorser.telly.features.playback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

/**
 * Per-second sampler behind the catch-up position readout: mounted only
 * while the info overlay is visible during catch-up, so the logic layer
 * never spins its own clock loop (seeks update the position directly).
 */
@Composable
internal fun PlaybackScreenCatchupTicker(viewModel: PlaybackViewModel) {
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.catchup.refreshPosition()
            delay(CATCHUP_POSITION_TICK_MS)
        }
    }
}

private const val CATCHUP_POSITION_TICK_MS = 1_000L
