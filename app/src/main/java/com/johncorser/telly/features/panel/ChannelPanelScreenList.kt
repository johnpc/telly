package com.johncorser.telly.features.panel

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import com.johncorser.telly.core.ui.stickyFocusGrab
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.launch

/**
 * The panel's channel rows: the focused row expands inline into the detail
 * card (round3 item 13) and DOWN at the last row wraps to the first — and
 * UP at the first to the last — exactly like TiviMate's guide (item 21,
 * re-verified on-device this round).
 */
@Composable
internal fun ChannelPanelScreenList(
    panel: PanelViewModel,
    playingChannelId: Long?,
    onTune: (ChannelEntity) -> Unit,
    onChannelMenu: (ChannelEntity) -> Unit,
) {
    val rows by panel.rows.collectAsState()
    val focusIndex by panel.focusIndex.collectAsState()
    val command by panel.focusCommand.collectAsState()
    var target by remember { mutableIntStateOf(panel.focusIndex.value) }
    val requester = remember { FocusRequester() }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = target)
    val scope = rememberCoroutineScope()

    fun wrapTo(index: Int) {
        scope.launch {
            listState.scrollToItem(index)
            target = index
        }
    }
    // Group switches / panel opens command the list to scroll + refocus
    // (per-group focus memory, capture 74 behavior).
    LaunchedEffect(command) {
        if (command.index != target) wrapTo(command.index)
    }
    LazyColumn(state = listState) {
        itemsIndexed(rows, key = { _, row -> row.channel.id }) { index, row ->
            Column {
                // The target row grabs focus through the shared
                // placement-gated sticky grab: a command-scrolled target is
                // subcomposed MID-MEASURE, where a raw requestFocus fires
                // before parents are placed and dies in the focus system's
                // bring-into-view coroutine (the group-tool lifecycle crash).
                ChannelPanelScreenRow(
                    row = row,
                    playing = row.channel.id == playingChannelId,
                    modifier =
                        (if (index == target) stickyFocusGrab(requester) else Modifier)
                            .onFocusChanged { if (it.isFocused) panel.onRowFocused(index) }
                            .onPreviewKeyEvent { event ->
                                wrapIndexFor(event, index, rows.lastIndex)?.let {
                                    wrapTo(it)
                                    true
                                } ?: false
                            },
                    onClick = { onTune(row.channel) },
                    onLongClick = { onChannelMenu(row.channel) },
                )
                if (index == focusIndex) ChannelPanelScreenDetail(row)
            }
        }
    }
}

/** The wrap destination a key press implies, or null for normal focus moves. */
private fun wrapIndexFor(
    event: KeyEvent,
    index: Int,
    lastIndex: Int,
): Int? {
    if (event.type != KeyEventType.KeyDown) return null
    return when {
        event.key == Key.DirectionDown && index == lastIndex && lastIndex > 0 -> 0
        event.key == Key.DirectionUp && index == 0 && lastIndex > 0 -> lastIndex
        else -> null
    }
}
