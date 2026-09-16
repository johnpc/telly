package com.johncorser.telly.features.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.johncorser.telly.R
import com.johncorser.telly.core.ui.OnboardingScreenMessage
import com.johncorser.telly.features.recording.RecordingScreenForm

/**
 * The search screen's transient overlays: the guide-cell dropdown a
 * programme row opens, the DVR form its Record rows push, and the
 * branded coming-soon placeholder the still-unbuilt rows share.
 */
@Composable
internal fun SearchScreenOverlay(
    overlay: SearchOverlay,
    viewModel: SearchViewModel,
) {
    when (overlay) {
        is SearchOverlay.ProgramMenu -> SearchScreenDropdown(viewModel, overlay.hit)
        is SearchOverlay.ComingSoon ->
            OnboardingScreenMessage(
                headline = overlay.feature,
                subtitle = stringResource(R.string.playback_coming_soon),
            )
        SearchOverlay.CustomRecording ->
            viewModel.programMenu.recording?.let { RecordingScreenForm(it) }
        SearchOverlay.None -> Unit
    }
}
