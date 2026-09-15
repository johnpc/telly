package com.johncorser.telly.features.playback

import androidx.compose.runtime.Composable
import com.johncorser.telly.features.panel.ChannelPanelScreenPin

/**
 * The blocked-channel PIN prompts over fullscreen playback, both reusing
 * the panel's centered PIN card: the tune gate (zap / panel row / restore
 * of a blocked channel) and the sheet's Block/Unblock confirmation.
 */
@Composable
internal fun PlaybackScreenBlockGate(
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ChannelPanelScreenPin(onSubmit = onSubmit, onDismiss = onDismiss)
}

/** The sheet's Block/Unblock PIN dialog; BACK pops back like any pushed screen. */
@Composable
internal fun PlaybackScreenBlockPinDialog(
    viewModel: PlaybackViewModel,
    dialog: PlaybackOverlay.BlockPin,
) {
    ChannelPanelScreenPin(
        onSubmit = viewModel.menu::submitBlockPin,
        title = dialog.mode.title,
    )
}
