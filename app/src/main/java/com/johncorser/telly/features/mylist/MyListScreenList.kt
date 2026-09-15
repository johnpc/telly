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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.johncorser.telly.core.ui.focusOnAppear

/** The My List rows; removing the focused entry re-lands focus on the first. */
@Composable
internal fun MyListScreenList(
    rows: List<MyListRow>,
    viewModel: MyListViewModel,
    onTuned: () -> Unit,
    onDescribe: (MyListRow) -> Unit,
) {
    val firstRowFocus = remember { FocusRequester() }
    var refocus by remember { mutableStateOf(false) }
    LaunchedEffect(rows) {
        if (refocus) {
            runCatching { firstRowFocus.requestFocus() }
            refocus = false
        }
    }
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(start = 40.dp, top = 80.dp, end = 40.dp, bottom = 44.dp),
    ) {
        items(rows, key = { MyListKeys.of(it.entry.channelKey, it.entry.startMs) }) { row ->
            val first = row == rows.first()
            MyListScreenRow(
                row = row,
                modifier =
                    Modifier
                        .focusRequester(if (first) firstRowFocus else remember { FocusRequester() })
                        .focusOnAppear(first),
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
