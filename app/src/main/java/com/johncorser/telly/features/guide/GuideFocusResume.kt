package com.johncorser.telly.features.guide

import com.johncorser.telly.core.kv.KeyValueStore
import com.johncorser.telly.features.playback.TuneController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Seeds the fresh grid's focus onto the last-tuned channel's row, so BACK
 * from fullscreen playback lands on the row OK left from instead of the top
 * of the list — [GuideGroupMemory]'s sibling for focus (the controller is
 * rebuilt on the fullscreen→back transition, losing the engine's state).
 * One-shot: it fires on the first non-empty rows emission and never again,
 * so a later group switch still resets focus to the group's first row. A
 * channel missing from the restored group's rows (or already at row 0)
 * keeps the engine's default initial focus.
 */
class GuideFocusResume(
    private val engine: GuideFocusEngine,
    private val store: KeyValueStore,
    private val visibleRows: () -> Int = { GuideGeometry.VISIBLE_ROWS },
) {
    fun arm(
        scope: CoroutineScope,
        rows: StateFlow<List<GuideRow>>,
        now: () -> Long,
    ) {
        val channelId = store.getLong(TuneController.LAST_CHANNEL_KEY) ?: return
        scope.launch {
            val list = rows.first { it.isNotEmpty() }
            val rowIndex = list.indexOfFirst { it.channel.id == channelId }
            if (rowIndex <= 0) return@launch
            val cell = GuideFocusNav.cellAt(list[rowIndex].cells, now()) ?: return@launch
            engine.apply(
                GuideFocusState(
                    focus = GuideFocus(rowIndex, cell, anchorMs = now()),
                    scrollX = engine.scrollX.value,
                    firstRow = GuideScrollPlanner.rowWindow(0, rowIndex, visibleRows(), list.size),
                ),
            )
        }
    }
}
