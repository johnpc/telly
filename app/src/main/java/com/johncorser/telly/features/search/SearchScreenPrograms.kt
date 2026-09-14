package com.johncorser.telly.features.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.R
import com.johncorser.telly.core.design.TELLY_CLOCK_BLUE
import com.johncorser.telly.core.ui.TellyScreenLogoTile
import com.johncorser.telly.features.search.SearchScreenDims as Dims

/**
 * The Programs list (captures 50/51): one row per programme match — channel
 * card at the left of the first row of each same-channel run, then title and
 * air time. Focusing a row feeds the right-side detail card; OK opens the
 * guide-cell dropdown.
 */
@Composable
internal fun SearchScreenPrograms(
    hits: List<SearchProgramHit>,
    viewModel: SearchViewModel,
) {
    SearchScreenHeader(R.string.search_programs)
    LazyColumn(
        contentPadding = PaddingValues(start = Dims.edgePad, top = Dims.shelfTop, bottom = Dims.edgePad),
        modifier = Modifier.width(Dims.listWidth),
    ) {
        items(hits) { hit ->
            SearchScreenProgramRow(hit, viewModel)
        }
    }
}

@Composable
private fun SearchScreenProgramRow(
    hit: SearchProgramHit,
    viewModel: SearchViewModel,
) {
    Row(Modifier.height(Dims.rowHeight)) {
        Box(Modifier.width(Dims.rowCardWidth), contentAlignment = Alignment.CenterStart) {
            if (hit.showsChannelCard) SearchScreenProgramChannel(hit)
        }
        Spacer(Modifier.width(Dims.rowTextStart))
        SearchScreenFocusRow(
            onClick = { viewModel.onProgramResult(hit) },
            modifier =
                Modifier
                    .fillMaxSize()
                    .onFocusChanged { if (it.isFocused) viewModel.onProgramFocused(hit) },
        ) {
            Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                // Airing rows tint the title light blue and append the dash
                // progress + remaining minutes to the times (live tm-03).
                Text(
                    text = hit.title,
                    color = if (hit.remaining != null) Color(TELLY_CLOCK_BLUE) else Color.Unspecified,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                SearchScreenAirTime(hit, fontSize = 14.sp)
            }
        }
    }
}

/** The per-run channel card at the row's left (logo + name, capture 50). */
@Composable
private fun SearchScreenProgramChannel(hit: SearchProgramHit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TellyScreenLogoTile(
            logoUrl = hit.channel.source.logoUrl,
            name = hit.channel.source.name,
            size = Dims.rowLogoHeight,
            modifier = Modifier.width(Dims.rowLogoWidth),
        )
        Text(
            text = hit.channel.source.name,
            color = Color.White,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
