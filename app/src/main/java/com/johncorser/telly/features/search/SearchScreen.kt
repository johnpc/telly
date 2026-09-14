package com.johncorser.telly.features.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.johncorser.telly.R
import com.johncorser.telly.core.design.TELLY_ONBOARDING_BACKGROUND
import com.johncorser.telly.core.ui.OnboardingScreenMessage
import com.johncorser.telly.features.settings.SettingsScreenPaywall

/**
 * The search screen (catalogue §4, captures 49-51). The reference overlays
 * the dimmed live video; telly's search is its own route over the app
 * background (single player engine). OK on a channel card tunes it via
 * [onTuned]; BACK closes overlays first, then the screen.
 */
@Composable
fun SearchScreen(
    deps: SearchDeps,
    onTuned: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val viewModel = remember { SearchViewModel(deps, scope) }
    val results by viewModel.results.collectAsState()
    val overlay by viewModel.overlay.collectAsState()
    BackHandler(enabled = overlay != SearchOverlay.None) { viewModel.dismissOverlay() }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(TELLY_ONBOARDING_BACKGROUND)),
    ) {
        Column(Modifier.fillMaxSize()) {
            SearchScreenTopBar(viewModel)
            if (results.isEmpty) {
                SearchScreenHistory(viewModel)
            } else {
                SearchScreenResults(results, viewModel, onTuned)
            }
        }
        SearchScreenOverlay(overlay, viewModel)
    }
}

/** Channels shelf on top, Programs list + focused detail card below. */
@Composable
private fun SearchScreenResults(
    results: SearchResults,
    viewModel: SearchViewModel,
    onTuned: () -> Unit,
) {
    val focused by viewModel.focusedProgram.collectAsState()
    if (results.channels.isNotEmpty()) {
        SearchScreenChannels(results.channels) { hit ->
            viewModel.onChannelResult(hit.channel)
            onTuned()
        }
    }
    if (results.programs.isNotEmpty()) {
        Row {
            Column(Modifier.weight(1f)) {
                SearchScreenPrograms(results.programs, viewModel)
            }
            Column(Modifier.align(Alignment.Top)) {
                focused?.let { SearchScreenDetail(it) }
            }
        }
    }
}

@Composable
private fun SearchScreenOverlay(
    overlay: SearchOverlay,
    viewModel: SearchViewModel,
) {
    when (overlay) {
        is SearchOverlay.ProgramMenu -> SearchScreenDropdown(viewModel)
        SearchOverlay.Paywall -> SettingsScreenPaywall(onClose = { viewModel.dismissOverlay() })
        is SearchOverlay.ComingSoon ->
            OnboardingScreenMessage(
                headline = overlay.feature,
                subtitle = stringResource(R.string.playback_coming_soon),
            )
        SearchOverlay.None -> Unit
    }
}
