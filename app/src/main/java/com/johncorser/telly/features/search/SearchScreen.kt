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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import com.johncorser.telly.core.design.TELLY_ONBOARDING_BACKGROUND
import com.johncorser.telly.features.playlist.db.ChannelEntity

/**
 * The search screen (catalogue §4, captures 49-51). The reference overlays
 * the dimmed live video; telly's search is its own route over the app
 * background (single player engine). OK on a channel or master card tunes
 * it via [onTuned]; the gear opens Settings via [onOpenSettings]; BACK
 * closes overlays first, then the screen.
 */
@Composable
fun SearchScreen(
    deps: SearchDeps,
    onTuned: () -> Unit,
    onOpenSettings: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val viewModel = remember { SearchViewModel(deps, scope) }
    val results by viewModel.results.collectAsState()
    val overlay by viewModel.overlays.current.collectAsState()
    // DOWN from the query bar lands on the last-visited node (round7 §C4
    // focus memory) or, fresh, on the FIRST result card — never Compose's
    // geometrically nearest one (ref-round6 07-search-down-from-querybar).
    val firstResult = remember { FocusRequester() }
    val restore = rememberSearchScreenRestore(viewModel)
    BackHandler(enabled = overlay != SearchOverlay.None) { viewModel.overlays.dismiss() }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(TELLY_ONBOARDING_BACKGROUND)),
    ) {
        Column(Modifier.fillMaxSize()) {
            SearchScreenTopBar(
                viewModel,
                downTargets = restore.downTargets(firstResult.takeIf { !results.isEmpty }),
                onOpenSettings = onOpenSettings,
            )
            if (results.isEmpty) {
                SearchScreenHistory(viewModel)
            } else {
                SearchScreenResults(results, viewModel, firstResult, restore, onTuned)
            }
        }
        SearchScreenOverlay(overlay, viewModel)
    }
}

/** Channels shelf on top, the two-pane Programs section + detail card below. */
@Composable
private fun SearchScreenResults(
    results: SearchResults,
    viewModel: SearchViewModel,
    firstResult: FocusRequester,
    restore: SearchScreenRestore,
    onTuned: () -> Unit,
) {
    val focused by viewModel.focusedProgram.collectAsState()
    val onTune: (ChannelEntity) -> Unit = { channel ->
        viewModel.onChannelResult(channel)
        onTuned()
    }
    if (results.channels.isNotEmpty()) {
        SearchScreenChannels(results.channels, firstResult, restore, viewModel) { hit -> onTune(hit.channel) }
    }
    if (results.programs.isNotEmpty()) {
        Row {
            Column(Modifier.weight(1f)) {
                SearchScreenPrograms(
                    groups = results.programs,
                    viewModel = viewModel,
                    firstFocus = firstResult.takeIf { results.channels.isEmpty() },
                    restore = restore,
                    onTuned = onTuned,
                )
            }
            Column(Modifier.align(Alignment.Top)) {
                focused?.let { SearchScreenDetail(it) }
            }
        }
    }
}
