package com.johncorser.telly.features.multiview

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

/** Panes positioned by [MultiviewGrid] fractions; 16:9 letterboxed inside. */
@Composable
internal fun MultiviewScreenGrid(
    viewModel: MultiviewViewModel,
    panes: List<MultiviewPane>,
    focusedId: Int,
    panesFocusable: Boolean,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val cells = MultiviewGrid.cells(panes.size)
        val requesters = remember { mutableMapOf<Int, FocusRequester>() }
        panes.forEachIndexed { index, pane ->
            val cell = cells[index]
            MultiviewScreenPane(
                pane = pane,
                focused = pane.id == focusedId,
                onFocused = { viewModel.panes.focus(pane.id) },
                onOk = { viewModel.onPaneOk(pane.id) },
                focusRequester = requesters.getOrPut(pane.id) { FocusRequester() },
                modifier =
                    Modifier
                        .offset(x = maxWidth * cell.x, y = maxHeight * cell.y)
                        .size(width = maxWidth * cell.w, height = maxHeight * cell.h),
            )
        }
        // D-pad focus returns to the focused pane whenever the grid is the
        // active layer again (menu/picker closed) or the grid reshapes.
        LaunchedEffect(panesFocusable, focusedId, panes.size) {
            while (panesFocusable && runCatching { requesters[focusedId]?.requestFocus() }.isFailure) {
                withFrameNanos { }
            }
        }
    }
}

/** CH+/- zap the focused pane in place, like fullscreen playback. */
internal fun onChannelZapKey(
    event: KeyEvent,
    viewModel: MultiviewViewModel,
): Boolean {
    if (event.type != KeyEventType.KeyDown) return false
    val delta =
        when (event.key) {
            Key.ChannelUp -> +1
            Key.ChannelDown -> -1
            else -> return false
        }
    viewModel.onChannelKey(delta)
    return true
}
