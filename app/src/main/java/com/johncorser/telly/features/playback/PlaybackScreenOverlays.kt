package com.johncorser.telly.features.playback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.johncorser.telly.R
import com.johncorser.telly.core.ui.OnboardingScreenMessage
import com.johncorser.telly.features.panel.ChannelPanelScreen

/** Renders whichever overlay is active over the fullscreen video. */
@Composable
internal fun PlaybackScreenOverlays(
    viewModel: PlaybackViewModel,
    overlay: PlaybackOverlay,
) {
    when (overlay) {
        PlaybackOverlay.None -> Unit
        PlaybackOverlay.Info -> PlaybackScreenInfoOverlay(viewModel)
        PlaybackOverlay.Panel -> PlaybackScreenPanel(viewModel)
        PlaybackOverlay.Menu -> PlaybackScreenPlayerMenu(viewModel)
        is PlaybackOverlay.ChannelMenu -> PlaybackScreenChannelMenu(viewModel)
        is PlaybackOverlay.ComingSoon ->
            OnboardingScreenMessage(
                headline = overlay.feature,
                subtitle = stringResource(R.string.playback_coming_soon),
            )
    }
}

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

@Composable
private fun PlaybackScreenPlayerMenu(viewModel: PlaybackViewModel) {
    val info by viewModel.info.collectAsState()
    val channel = viewModel.menu.menuChannel()
    PlaybackScreenMenu(
        sections = PlayerMenu.sections(info?.title, channel?.source?.name.orEmpty()),
        favorite = channel?.flags?.favorite == true,
        onItem = viewModel.menu::onMenuItem,
    )
}

@Composable
private fun PlaybackScreenChannelMenu(viewModel: PlaybackViewModel) {
    val channel = viewModel.menu.menuChannel()
    PlaybackScreenMenu(
        sections = PlayerMenu.channelSections(channel?.source?.name.orEmpty()),
        favorite = channel?.flags?.favorite == true,
        onItem = viewModel.menu::onMenuItem,
    )
}
