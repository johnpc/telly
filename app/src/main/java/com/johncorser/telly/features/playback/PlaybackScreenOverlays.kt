package com.johncorser.telly.features.playback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.johncorser.telly.features.panel.ChannelPanelScreen

/**
 * Renders whichever overlay is active over the fullscreen video, with
 * TiviMate's measured motion (round3 items 6/18): info fades in ~350 ms with
 * a 12 px upward settle and exits instantly; the panel cross-fades ~150 ms
 * both ways; the zap overlay pops instantly. The panel stays composed behind
 * the channel-row menu sheet (item 14).
 */
@Composable
internal fun PlaybackScreenOverlays(
    viewModel: PlaybackViewModel,
    overlay: PlaybackOverlay,
) {
    val info = overlay == PlaybackOverlay.Info || overlay == PlaybackOverlay.InfoTransport
    AnimatedVisibility(
        visible = info,
        enter =
            fadeIn(tween(INFO_FADE_MS, easing = LinearEasing)) +
                slideInVertically(tween(INFO_FADE_MS)) { INFO_SLIDE_PX },
        exit = fadeOut(snap()),
    ) {
        PlaybackScreenInfoOverlay(viewModel, transport = overlay == PlaybackOverlay.InfoTransport)
    }
    AnimatedVisibility(
        visible = overlay == PlaybackOverlay.ZapInfo,
        enter = fadeIn(snap()),
        exit = fadeOut(snap()),
    ) {
        PlaybackScreenZapOverlay(viewModel)
    }
    AnimatedVisibility(
        visible = overlay == PlaybackOverlay.Panel || overlay is PlaybackOverlay.ChannelMenu,
        enter = fadeIn(tween(PANEL_FADE_MS)),
        exit = fadeOut(tween(PANEL_FADE_MS)),
    ) {
        PlaybackScreenPanel(viewModel)
    }
    // Sheet backdrop dim: settles with the sheet's entrance, outlives its
    // instant cut with a ~300 ms fade-out (ref-round6 §A).
    PlaybackScreenMenuScrim(visible = overlay is PlaybackOverlay.ChannelMenu)
    PlaybackScreenMenuLayers(viewModel, overlay)
}

private const val INFO_FADE_MS = 350
private const val INFO_SLIDE_PX = 12
private const val PANEL_FADE_MS = 150

@Composable
private fun PlaybackScreenPanel(viewModel: PlaybackViewModel) {
    val current by viewModel.current.collectAsState()
    ChannelPanelScreen(
        panel = viewModel.panel,
        playingChannelId = current?.id,
        onTune = viewModel::tuneFromPanel,
        onChannelMenu = viewModel::showChannelMenu,
    )
}
