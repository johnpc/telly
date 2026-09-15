package com.johncorser.telly.features.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.johncorser.telly.R
import com.johncorser.telly.core.ui.OnboardingScreenMessage

/**
 * The search screen's transient overlays: the guide-cell dropdown a
 * programme row opens, and the branded coming-soon placeholder that the
 * voice orb and formerly-premium rows share.
 */
@Composable
internal fun SearchScreenOverlay(
    overlay: SearchOverlay,
    viewModel: SearchViewModel,
) {
    when (overlay) {
        is SearchOverlay.ProgramMenu -> SearchScreenDropdown(viewModel)
        is SearchOverlay.ComingSoon ->
            OnboardingScreenMessage(
                headline = overlay.feature,
                subtitle = stringResource(R.string.playback_coming_soon),
            )
        SearchOverlay.None -> Unit
    }
}
