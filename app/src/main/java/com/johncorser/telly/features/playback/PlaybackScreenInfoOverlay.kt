package com.johncorser.telly.features.playback

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.johncorser.telly.core.ui.TellyScreenProgressBar

/**
 * Bottom info overlay (capture 34 + round3-ref 02): top scrim with group +
 * clock, channel logo, programme lines, full-width progress bar, optional
 * transport row (second UP, round3-ref 03b) and the shortcut cards row —
 * TV guide · History · recent-channel cards · Clear (history-round2 §1).
 */
@Composable
internal fun PlaybackScreenInfoOverlay(
    viewModel: PlaybackViewModel,
    transport: Boolean,
) {
    val info by viewModel.info.collectAsState()
    val recents by viewModel.recents.cards.collectAsState()
    val catchupState by viewModel.catchup.state.collectAsState()
    var focusedRecent by remember { mutableStateOf<RecentCard?>(null) }
    if (catchupState != null) {
        // The visible overlay drives the catch-up position readout.
        PlaybackScreenCatchupTicker(viewModel)
    }
    val data = info ?: return
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
            PlaybackScreenTransportRow(
                data = data,
                onFeature = viewModel::showComingSoon,
                onSeek = if (catchupState != null) viewModel.catchup::seekBy else null,
                skip = viewModel.catchup.skip(),
            )
        }
        Spacer(Modifier.height(30.dp))
        Box(Modifier.fillMaxWidth()) {
            PlaybackScreenCards(
                recents = recents,
                actions =
                    PlaybackCardActions(
                        onGuide = { viewModel.exitToGuide() },
                        onHistory = { viewModel.openHistory() },
                        onMultiview = { viewModel.openMultiview() },
                        onRecent = { card -> viewModel.recents.tune(card) },
                        onClear = { viewModel.recents.clear() },
                    ),
                onRecentFocus = { card, focused ->
                    focusedRecent = if (focused) card else focusedRecent.takeIf { it != card }
                },
            )
            PlaybackScreenCardsFooter(recentCardLine(focusedRecent))
        }
        Spacer(Modifier.height(19.dp))
    }
}
