package com.johncorser.telly.features.playback

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import com.johncorser.telly.R
import com.johncorser.telly.core.ui.TellyScreenProgressBar

/**
 * Bottom info overlay (capture 34 + round3-ref 02): top scrim with group +
 * clock, channel logo, programme lines, full-width progress bar, optional
 * transport row (second UP, round3-ref 03b) and the shortcut cards.
 */
@Composable
internal fun PlaybackScreenInfoOverlay(
    viewModel: PlaybackViewModel,
    transport: Boolean,
) {
    val info by viewModel.info.collectAsState()
    val data = info ?: return
    val historyLabel = stringResource(R.string.playback_card_history)
    PlaybackScreenOverlayScaffold(data.group, data.clockText) {
        PlaybackScreenInfoBlock(data, showBadges = true, showDescription = false)
        Spacer(Modifier.height(10.dp))
        TellyScreenProgressBar(
            permille = data.progressPermille,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
            thumb = true,
        )
        if (transport) {
            PlaybackScreenTransportRow(data) { feature -> viewModel.showComingSoon(feature) }
        }
        Spacer(Modifier.height(30.dp))
        Box(Modifier.fillMaxWidth()) {
            PlaybackScreenCards(
                onGuide = { viewModel.exitToGuide() },
                onHistory = { viewModel.showComingSoon(historyLabel) },
            )
            // The chevron sits centered at y≈1036 px, overlapping the card
            // row's bottom edge (round3-ref 02 + item 20).
            Icon(
                painter = painterResource(R.drawable.ic_chevron_down),
                contentDescription = null,
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 11.dp)
                        .size(28.dp),
                tint = Color.White.copy(alpha = 0.9f),
            )
        }
        Spacer(Modifier.height(19.dp))
    }
}
