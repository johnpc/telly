package com.johncorser.telly.features.playback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.johncorser.telly.R
import com.johncorser.telly.core.ui.OnboardingScreenMessage
import com.johncorser.telly.features.guide.GuideScreenChannelOptionsPane
import com.johncorser.telly.features.playback.tracks.PlaybackScreenTrackPicker
import com.johncorser.telly.features.playlist.db.displayName
import com.johncorser.telly.features.recording.RecordingScreenForm
import com.johncorser.telly.features.recording.RecordingScreenStopConfirm

/**
 * The quick-bar, the long-OK sheet and the screens its rows push, hosted in
 * the shared surface switch: Channel options cross-fades in place of the
 * sheet on push and fades out on pop straight to the panel (ref-round6 §A);
 * everything else swaps instantly.
 */
@Composable
internal fun PlaybackScreenMenuLayers(
    viewModel: PlaybackViewModel,
    overlay: PlaybackOverlay,
) {
    PlaybackScreenMenuSurfaceSwitch(overlay, ::playbackMenuSurface) { active ->
        when (active) {
            PlaybackOverlay.QuickBar -> PlaybackScreenQuickBar(viewModel)
            is PlaybackOverlay.ChannelMenu -> PlaybackScreenChannelMenu(viewModel, active.channelId)
            is PlaybackOverlay.Description ->
                OnboardingScreenMessage(headline = active.title, subtitle = active.text)
            is PlaybackOverlay.ChannelOptions ->
                GuideScreenChannelOptionsPane(
                    channel = active.channel,
                    options = viewModel.menu.actions.options,
                    onRow = viewModel.menu::onChannelOption,
                )
            is PlaybackOverlay.TrackPicker -> PlaybackScreenTrackPicker(viewModel.trackPickers, active.kind)
            is PlaybackOverlay.ComingSoon ->
                OnboardingScreenMessage(
                    headline = active.feature,
                    subtitle = stringResource(R.string.playback_coming_soon),
                )
            is PlaybackOverlay.RecordingStop ->
                RecordingScreenStopConfirm(
                    channelName = active.channelName,
                    onStop = { viewModel.recordingMenu?.confirmStop(active.recordingId) },
                    onDismiss = { viewModel.onKey(PlaybackKey.BACK) },
                )
            is PlaybackOverlay.CustomRecording -> viewModel.recordingMenu?.let { RecordingScreenForm(it) }
            is PlaybackOverlay.BlockPin -> PlaybackScreenBlockPinDialog(viewModel, active)
            else -> Unit
        }
    }
}

private fun playbackMenuSurface(overlay: PlaybackOverlay): PlayerMenuSurface =
    when (overlay) {
        is PlaybackOverlay.ChannelMenu -> PlayerMenuSurface.SHEET
        is PlaybackOverlay.ChannelOptions -> PlayerMenuSurface.CHANNEL_OPTIONS
        is PlaybackOverlay.Pushed -> PlayerMenuSurface.PUSHED
        else -> PlayerMenuSurface.NONE
    }

/** Long-OK on a panel row: the FULL sheet over the still-visible panel. */
@Composable
private fun PlaybackScreenChannelMenu(
    viewModel: PlaybackViewModel,
    channelId: Long,
) {
    val channel = viewModel.menu.menuChannel()
    val myListKeys by viewModel.myList.keys.collectAsState()
    PlaybackScreenMenu(
        sections = PlayerMenu.sections(viewModel.panel.nowTitleOf(channelId), channel?.displayName.orEmpty()),
        favorite = channel?.flags?.favorite == true,
        onItem = viewModel.menu::onMenuItem,
        restore = viewModel.menu.sheetFocus.restore,
        inMyList = channel != null && viewModel.menu.myList?.savedFor(channel, myListKeys) == true,
        blocked = channel?.flags?.blocked == true,
    )
}
