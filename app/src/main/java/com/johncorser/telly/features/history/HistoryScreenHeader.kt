package com.johncorser.telly.features.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.johncorser.telly.R
import com.johncorser.telly.core.ui.TellyScreenIconCircle
import com.johncorser.telly.core.ui.TellyScreenWhiteText
import com.johncorser.telly.core.ui.focusOnAppear

/**
 * Top-right header (history-round2 uidump 12, 2 px = 1 dp): "History" at
 * y 20 with the 40 dp trash icon 12 dp to its right, 24 dp off the edge.
 * The trash takes focus only on the empty screen (nothing else can).
 */
@Composable
internal fun HistoryScreenHeader(
    viewModel: HistoryViewModel,
    focusTrash: Boolean,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, end = 24.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TellyScreenWhiteText(text = stringResource(R.string.history_title), fontSize = 28.sp)
        Spacer(Modifier.width(12.dp))
        TellyScreenIconCircle(
            icon = R.drawable.ic_search_trash,
            onClick = viewModel::clearAll,
            modifier = Modifier.focusOnAppear(focusTrash),
            size = 40.dp,
            iconSize = 20.dp,
            contentDescription = stringResource(R.string.history_clear_all),
        )
    }
}
