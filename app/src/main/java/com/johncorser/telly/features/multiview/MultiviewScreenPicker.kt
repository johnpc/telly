package com.johncorser.telly.features.multiview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.johncorser.telly.core.ui.TellyScreenWhiteText
import com.johncorser.telly.core.ui.focusOnAppear
import com.johncorser.telly.core.ui.rememberFocusSeed
import com.johncorser.telly.features.panel.ChannelPanelScreenRow
import com.johncorser.telly.features.panel.PanelRow

/**
 * The channel picker (multiview-round 05/08/10) over the dimmed panes:
 * channel list left (the shared panel rows; the pane's channel keeps the
 * play arrow), the focused channel's schedule in the middle, its airing
 * programme's detail card top-right. OK on a row commits the pick.
 */
@Composable
internal fun MultiviewScreenPicker(viewModel: MultiviewViewModel) {
    val rows by viewModel.picker.panel.rows.collectAsState()
    val schedule by viewModel.picker.schedule.collectAsState()
    val detail by viewModel.picker.focusedRow.collectAsState()
    val panes by viewModel.panes.panes.collectAsState()
    val focusedId by viewModel.panes.focusedId.collectAsState()
    val playingId = panes.firstOrNull { it.id == focusedId }?.channel?.id
    Row(
        Modifier
            .fillMaxSize()
            .background(Color(PICKER_SCRIM)),
    ) {
        MultiviewScreenPickerList(viewModel, rows, playingId, Modifier.width(LIST_WIDTH).fillMaxHeight())
        MultiviewScreenPickerSchedule(
            schedule,
            Modifier
                .weight(1f)
                .padding(top = 16.dp, start = 14.dp),
        )
        Column(Modifier.width(DETAIL_WIDTH).padding(top = 36.dp, end = 40.dp)) {
            detail?.let { MultiviewScreenPickerDetail(it) }
        }
    }
}

@Composable
private fun MultiviewScreenPickerList(
    viewModel: MultiviewViewModel,
    rows: List<PanelRow>,
    playingId: Long?,
    modifier: Modifier,
) {
    val panel = viewModel.picker.panel
    val initialIndex = remember { panel.focusIndex.value.coerceAtLeast(0) }
    val seed = rememberFocusSeed()
    Column(modifier.padding(start = 8.dp, top = 8.dp)) {
        TellyScreenWhiteText("All channels", fontSize = 16.sp)
        LazyColumn(
            modifier = seed.modifier(),
            state = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex),
        ) {
            itemsIndexed(rows, key = { _, row -> row.channel.id }) { index, row ->
                ChannelPanelScreenRow(
                    row = row,
                    playing = row.channel.id == playingId,
                    modifier =
                        Modifier
                            .onFocusChanged { if (it.isFocused) panel.onRowFocused(index) }
                            .focusOnAppear(index == initialIndex, seed.seeded),
                    onClick = { viewModel.onPick(row.channel) },
                )
            }
        }
    }
}

private const val PICKER_SCRIM = 0x99000000
private val LIST_WIDTH = 300.dp
private val DETAIL_WIDTH = 328.dp
