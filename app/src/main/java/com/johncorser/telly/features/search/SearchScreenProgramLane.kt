package com.johncorser.telly.features.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.core.ui.TellyScreenLogoTile
import com.johncorser.telly.features.search.SearchScreenDims as Dims

/**
 * The Programs master lane (ref-round6 §D / 08-programs-card-*): one
 * adjacent 120x103 dp card per matching channel — logo tile over the
 * channel name, no airing line, no progress. Focusing a card selects it in
 * the ViewModel, which swaps the airings pane beside it; the selected card
 * keeps a grey outline while unfocused (round7 tm-search-news) and OK opens
 * the shared Unlock Premium screen (round7 device check — not a tune).
 */
@Composable
internal fun SearchScreenProgramLane(
    groups: List<SearchProgramChannel>,
    selected: SearchProgramChannel?,
    viewModel: SearchViewModel,
    restore: SearchScreenRestore,
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = Dims.edgePad),
        modifier = Modifier.width(Dims.rowCardWidth),
    ) {
        items(groups, key = { group -> group.channel.id }) { group ->
            SearchScreenProgramChannelCard(
                group = group,
                selected = group.channel.id == selected?.channel?.id,
                viewModel = viewModel,
                restore = restore,
            )
        }
    }
}

@Composable
private fun SearchScreenProgramChannelCard(
    group: SearchProgramChannel,
    selected: Boolean,
    viewModel: SearchViewModel,
    restore: SearchScreenRestore,
) {
    SearchScreenFocusRow(
        onClick = viewModel::onProgramChannelResult,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(Dims.masterCardHeight)
                .searchResultsNode(
                    memory = viewModel.focusMemory,
                    restore = restore,
                    node = SearchFocusMemory.Node.MasterCard(group.channel.id),
                ).onFocusChanged { if (it.isFocused) viewModel.onProgramChannelFocused(group) },
        dimWhenResting = true,
        restingOutline = selected,
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(vertical = Dims.masterCardPad),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TellyScreenLogoTile(
                logoUrl = group.channel.source.logoUrl,
                name = group.channel.source.name,
                size = Dims.rowLogoHeight,
                modifier = Modifier.width(Dims.rowLogoWidth),
            )
            Spacer(Modifier.height(Dims.masterNameGap))
            Text(
                text = group.channel.source.name,
                color = Color.White,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
