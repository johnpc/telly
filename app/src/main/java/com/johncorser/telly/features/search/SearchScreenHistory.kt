package com.johncorser.telly.features.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.R
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED
import com.johncorser.telly.core.ui.TellyScreenIconCircle
import com.johncorser.telly.features.search.SearchScreenDims as Dims

/**
 * The idle landing pane (capture 49): "Search history" header aligned with
 * the query bar, trash icon at the bar's right edge, then either the entry
 * rows or a centered "No history".
 */
@Composable
internal fun SearchScreenHistory(viewModel: SearchViewModel) {
    val entries by viewModel.history.collectAsState()
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .padding(start = Dims.historyStart, top = Dims.headerTop)
                .width(Dims.barWidth),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SearchScreenHeader(R.string.search_history, Modifier.weight(1f))
            TellyScreenIconCircle(
                icon = R.drawable.ic_search_trash,
                onClick = viewModel::clearHistory,
                size = 36.dp,
                iconSize = 18.dp,
            )
        }
        if (entries.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.search_no_history),
                    color = Color(TELLY_TEXT_MUTED),
                    fontSize = 18.sp,
                )
            }
        } else {
            LazyColumn(Modifier.padding(start = Dims.historyStart, top = 8.dp)) {
                items(entries) { entry ->
                    SearchScreenTextRow(
                        text = entry,
                        onClick = { viewModel.onHistoryEntry(entry) },
                        modifier = Modifier.width(Dims.barWidth),
                    )
                }
            }
        }
    }
}
