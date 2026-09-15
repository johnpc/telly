package com.johncorser.telly.features.vod

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.johncorser.telly.core.design.TELLY_ONBOARDING_BACKGROUND
import com.johncorser.telly.features.panel.ChannelPanelScreenGroups
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * The Movies browser (ux-spec §VOD, premium/uncaptured -> telly's design
 * language): categories column left (the panel's groups idiom), logo-art
 * item cards right, BACK popping to the guide. Empty playlists render the
 * "No VOD content" state.
 */
@Composable
fun VodScreen(
    deps: VodDeps,
    onPlay: (String) -> Unit,
) {
    val scope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
    val viewModel = remember { VodViewModel(deps, scope) }
    DisposableEffect(Unit) { onDispose { scope.cancel() } }
    val empty = viewModel.empty.collectAsState().value
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(TELLY_ONBOARDING_BACKGROUND)),
    ) {
        VodScreenTitle()
        if (empty) {
            VodScreenEmpty(Modifier.align(Alignment.Center))
        } else {
            VodScreenBrowser(viewModel, onPlay)
        }
    }
}

/** Categories column + the selected category's card grid. */
@Composable
private fun VodScreenBrowser(
    viewModel: VodViewModel,
    onPlay: (String) -> Unit,
) {
    val categories = viewModel.categories.collectAsState().value
    val selected = viewModel.selected.collectAsState().value
    val cards = viewModel.cards.collectAsState().value
    Row(Modifier.fillMaxSize().padding(top = 80.dp)) {
        ChannelPanelScreenGroups(
            groups = categories,
            selected = selected.orEmpty(),
            onSelect = viewModel::select,
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 156.dp),
            modifier = Modifier.fillMaxSize().padding(start = 8.dp, end = 40.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(cards, key = { _, card -> card.item.itemKey }) { index, card ->
                VodScreenCard(
                    card = card,
                    onClick = { onPlay(card.item.itemKey) },
                    requestFocus = index == 0,
                )
            }
        }
    }
}
