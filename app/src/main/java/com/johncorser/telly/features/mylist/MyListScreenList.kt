package com.johncorser.telly.features.mylist

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.johncorser.telly.core.ui.focusOnAppear
import com.johncorser.telly.core.ui.rememberFocusScreenReclaim
import com.johncorser.telly.core.ui.rememberFocusSeed

/** The My List rows; removing the focused entry re-lands focus on the first. */
@Composable
internal fun MyListScreenList(
    rows: List<MyListRow>,
    viewModel: MyListViewModel,
    onTuned: () -> Unit,
    onDescribe: (MyListRow) -> Unit,
) {
    val reclaim = rememberFocusScreenReclaim()
    val seed = rememberFocusSeed()
    var refocus by remember { mutableStateOf(false) }
    // The reclaim waits for the shrunk list to land: grabbing on the
    // long-click itself would target the row list mid-removal.
    LaunchedEffect(rows) {
        if (refocus) {
            reclaim.reclaim()
            refocus = false
        }
    }
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(start = 40.dp, top = 80.dp, end = 40.dp, bottom = 44.dp)
            .then(seed.modifier()),
    ) {
        items(rows, key = { MyListKeys.of(it.entry.channelKey, it.entry.startMs) }) { row ->
            val first = row == rows.first()
            MyListScreenRow(
                row = row,
                modifier =
                    (if (first) reclaim.target() else Modifier)
                        .focusOnAppear(first, seed.seeded),
                onClick = {
                    if (row.airing) {
                        viewModel.tune(row)
                        onTuned()
                    } else {
                        onDescribe(row)
                    }
                },
                onLongClick = {
                    viewModel.remove(row)
                    refocus = true
                },
            )
        }
    }
}
