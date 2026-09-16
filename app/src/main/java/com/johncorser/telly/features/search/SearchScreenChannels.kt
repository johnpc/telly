package com.johncorser.telly.features.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.R
import com.johncorser.telly.core.ui.LocalAccentColor
import com.johncorser.telly.core.ui.TellyScreenLogoTile
import com.johncorser.telly.core.ui.TellyScreenProgressBar
import com.johncorser.telly.features.playlist.db.displayName
import com.johncorser.telly.features.search.SearchScreenDims as Dims

/**
 * The Channels shelf (capture 50): landscape cards with logo tile, channel
 * name, the airing programme in accent blue and a thin progress line.
 * OK tunes the channel (the free reference returns straight to playback).
 */
@Composable
internal fun SearchScreenChannels(
    hits: List<SearchChannelHit>,
    firstFocus: FocusRequester?,
    restore: SearchScreenRestore,
    viewModel: SearchViewModel,
    onTune: (SearchChannelHit) -> Unit,
) {
    SearchScreenHeader(R.string.search_channels)
    LazyRow(
        contentPadding = PaddingValues(horizontal = Dims.edgePad),
        horizontalArrangement = Arrangement.spacedBy(Dims.cardGap),
        modifier = Modifier.padding(top = Dims.shelfTop),
    ) {
        itemsIndexed(hits, key = { _, hit -> hit.channel.id }) { index, hit ->
            SearchScreenChannelCard(
                hit = hit,
                onClick = { onTune(hit) },
                // DOWN from the query bar targets the first card fresh
                // (round6 07) or the last-visited one (round7 §C4).
                modifier =
                    Modifier
                        .then(if (index == 0 && firstFocus != null) Modifier.focusRequester(firstFocus) else Modifier)
                        .searchResultsNode(
                            memory = viewModel.focusMemory,
                            restore = restore,
                            node = SearchFocusMemory.Node.ChannelCard(hit.channel.id),
                        ),
            )
        }
    }
}

@Composable
private fun SearchScreenChannelCard(
    hit: SearchChannelHit,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    SearchScreenFocusRow(
        onClick = onClick,
        modifier = modifier.width(Dims.cardWidth),
        restingContainer = Dims.cardFill,
        dimWhenResting = true,
    ) {
        Column(Modifier.padding(Dims.cardPad), horizontalAlignment = Alignment.CenterHorizontally) {
            TellyScreenLogoTile(
                logoUrl = hit.channel.source.logoUrl,
                name = hit.channel.displayName,
                size = Dims.logoHeight,
                modifier = Modifier.width(Dims.logoWidth),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = hit.channel.displayName,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = hit.nowTitle.orEmpty(),
                color = LocalAccentColor.current,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(4.dp))
            TellyScreenProgressBar(permille = hit.progressPermille, modifier = Modifier.fillMaxWidth())
        }
    }
}
