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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.core.ui.TellyScreenLogoTile
import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.search.SearchScreenDims as Dims

/**
 * The Programs master lane (ref-round6 §D / 08-programs-card-*): one
 * adjacent 120x103 dp card per matching channel — logo tile over the
 * channel name, no airing line, no progress. Focusing a card selects it in
 * the ViewModel, which swaps the airings pane beside it.
 */
@Composable
internal fun SearchScreenProgramLane(
    groups: List<SearchProgramChannel>,
    viewModel: SearchViewModel,
    firstFocus: FocusRequester?,
    onTune: (ChannelEntity) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = Dims.edgePad),
        modifier = Modifier.width(Dims.rowCardWidth),
    ) {
        itemsIndexed(groups, key = { _, group -> group.channel.id }) { index, group ->
            SearchScreenProgramChannelCard(
                group = group,
                viewModel = viewModel,
                onTune = onTune,
                modifier = if (index == 0 && firstFocus != null) Modifier.focusRequester(firstFocus) else Modifier,
            )
        }
    }
}

@Composable
private fun SearchScreenProgramChannelCard(
    group: SearchProgramChannel,
    viewModel: SearchViewModel,
    onTune: (ChannelEntity) -> Unit,
    modifier: Modifier,
) {
    SearchScreenFocusRow(
        // OK on a master card is uncaptured in round6; tuning matches the
        // Channels-shelf card semantics until a device round says otherwise.
        onClick = { onTune(group.channel) },
        modifier =
            modifier
                .fillMaxWidth()
                .height(Dims.masterCardHeight)
                .onFocusChanged { if (it.isFocused) viewModel.onProgramChannelFocused(group) },
        dimWhenResting = true,
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
