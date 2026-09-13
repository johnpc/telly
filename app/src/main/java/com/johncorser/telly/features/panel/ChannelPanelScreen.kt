package com.johncorser.telly.features.panel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_CLOCK_BLUE
import com.johncorser.telly.core.design.TELLY_ONBOARDING_BACKGROUND
import com.johncorser.telly.features.playlist.db.ChannelEntity

/**
 * Channel-list panel over the dimmed video (captures 25/36/47): groups
 * column left, channel rows right with now-programme + progress, a detail
 * card for the focused row, OK tunes, long-OK opens the channel menu.
 */
@Composable
fun ChannelPanelScreen(
    panel: PanelViewModel,
    playingChannelId: Long?,
    onTune: (ChannelEntity) -> Unit,
    onChannelMenu: (ChannelEntity) -> Unit,
) {
    val groups by panel.groups.collectAsState()
    val selected by panel.selectedGroup.collectAsState()
    val rows by panel.rows.collectAsState()
    val clockText by panel.clockText.collectAsState()
    val focusIndex by panel.focusIndex.collectAsState()
    Row(
        Modifier
            .fillMaxSize()
            .background(Color(TELLY_ONBOARDING_BACKGROUND).copy(alpha = 0.9f)),
    ) {
        ChannelPanelScreenGroups(groups, selected, panel::selectGroup)
        Column(Modifier.weight(1f).padding(top = 8.dp, end = 16.dp)) {
            Text(
                text = clockText,
                modifier = Modifier.padding(start = 12.dp),
                color = Color(TELLY_CLOCK_BLUE),
                fontSize = 14.sp,
            )
            ChannelPanelScreenDetail(rows.getOrNull(focusIndex))
            ChannelPanelScreenList(panel, rows, playingChannelId, onTune, onChannelMenu)
        }
    }
}

@Composable
private fun ChannelPanelScreenList(
    panel: PanelViewModel,
    rows: List<PanelRow>,
    playingChannelId: Long?,
    onTune: (ChannelEntity) -> Unit,
    onChannelMenu: (ChannelEntity) -> Unit,
) {
    val initialFocus = remember { panel.focusIndex.value }
    val requester = remember { FocusRequester() }
    LazyColumn(state = rememberLazyListState(initialFirstVisibleItemIndex = initialFocus)) {
        itemsIndexed(rows, key = { _, row -> row.channel.id }) { index, row ->
            ChannelPanelScreenRow(
                row = row,
                playing = row.channel.id == playingChannelId,
                modifier =
                    (if (index == initialFocus) Modifier.focusRequester(requester) else Modifier)
                        .onFocusChanged { if (it.isFocused) panel.onRowFocused(index) },
                onClick = { onTune(row.channel) },
                onLongClick = { onChannelMenu(row.channel) },
            )
            if (index == initialFocus) {
                LaunchedEffect(Unit) { requester.requestFocus() }
            }
        }
    }
}
