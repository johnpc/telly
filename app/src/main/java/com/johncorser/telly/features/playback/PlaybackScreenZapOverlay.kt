package com.johncorser.telly.features.playback

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Compact zap overlay (round3-ref 10): shown the moment a channel change
 * starts, over the still-visible previous frame — logo, title, times line,
 * description and next programme. No progress bar, cards or chevron.
 */
@Composable
internal fun PlaybackScreenZapOverlay(viewModel: PlaybackViewModel) {
    val info by viewModel.info.collectAsState()
    val data = info ?: return
    PlaybackScreenOverlayScaffold(data.group, data.clockText) {
        PlaybackScreenInfoBlock(data, showBadges = false, showDescription = true)
        Spacer(Modifier.height(24.dp))
    }
}
