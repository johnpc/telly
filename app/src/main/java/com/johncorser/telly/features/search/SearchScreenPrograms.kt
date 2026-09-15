package com.johncorser.telly.features.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.R
import com.johncorser.telly.core.design.TELLY_CLOCK_BLUE
import com.johncorser.telly.features.search.SearchScreenDims as Dims

/**
 * The Programs section (ref-round6 §D): a channel-master / airings-detail
 * two-pane. The left lane lists ONE card per matching channel in
 * case-insensitive name order; the rows pane lists ONLY the selected
 * channel's airings, chronological, one row per airing, never deduped.
 * Focusing a card selects it (ViewModel state) and swaps the rows pane,
 * preselecting that channel's first airing into the detail card.
 */
@Composable
internal fun SearchScreenPrograms(
    groups: List<SearchProgramChannel>,
    viewModel: SearchViewModel,
    firstFocus: FocusRequester?,
    restore: SearchScreenRestore,
    onTuned: () -> Unit,
) {
    SearchScreenHeader(R.string.search_programs)
    val selected by viewModel.selectedChannel.collectAsState()
    Row(Modifier.padding(start = Dims.edgePad, top = Dims.programsTop)) {
        SearchScreenProgramLane(groups, selected, viewModel, restore, onTuned)
        Spacer(Modifier.width(Dims.rowTextStart))
        // With no Channels shelf, DOWN from the query bar lands on the
        // FIRST AIRING ROW, not the master card (round7 device check).
        SearchScreenAiringsPane(selected?.airings.orEmpty(), viewModel, firstFocus, restore)
    }
}

/** The rows pane: the selected channel's airings only, chronological. */
@Composable
private fun SearchScreenAiringsPane(
    airings: List<SearchProgramHit>,
    viewModel: SearchViewModel,
    firstFocus: FocusRequester?,
    restore: SearchScreenRestore,
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = Dims.edgePad),
        modifier = Modifier.width(Dims.rowsWidth),
    ) {
        itemsIndexed(airings) { index, hit ->
            SearchScreenProgramRow(
                hit = hit,
                viewModel = viewModel,
                isLast = index == airings.lastIndex,
                focus = firstFocus.takeIf { index == 0 },
                restore = restore,
            )
        }
    }
}

@Composable
private fun SearchScreenProgramRow(
    hit: SearchProgramHit,
    viewModel: SearchViewModel,
    isLast: Boolean,
    focus: FocusRequester?,
    restore: SearchScreenRestore,
) {
    SearchScreenFocusRow(
        onClick = { viewModel.onProgramResult(hit) },
        modifier =
            Modifier
                .fillMaxWidth()
                .height(Dims.rowHeight)
                .then(if (focus != null) Modifier.focusRequester(focus) else Modifier)
                .searchResultsNode(
                    memory = viewModel.focusMemory,
                    restore = restore,
                    node = SearchFocusMemory.Node.AiringRow(hit.channel.id, hit.program.startMs),
                ).onFocusChanged { if (it.isFocused) viewModel.onProgramFocused(hit) }
                // DOWN stops dead at the selected channel's last airing
                // (ref-round6 §D) instead of leaking into the master lane.
                .focusProperties { if (isLast) down = FocusRequester.Cancel },
        dimWhenResting = true,
    ) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            // Airing rows tint the title light blue and append the dash
            // progress + remaining minutes to the times (live tm-03).
            Text(
                text = hit.title,
                color = if (hit.remaining != null) Color(TELLY_CLOCK_BLUE) else Color.Unspecified,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            SearchScreenAirTime(hit, fontSize = 13.sp)
        }
    }
}
