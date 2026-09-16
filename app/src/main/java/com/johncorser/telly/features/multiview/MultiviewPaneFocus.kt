package com.johncorser.telly.features.multiview

import androidx.compose.runtime.mutableStateOf
import com.johncorser.telly.core.ui.grabFocusUntilLanded

/**
 * Pane-focus bookkeeping for the multiview grid: which pane currently owns
 * D-pad focus and which panes are placed, feeding the shared
 * placement-gated bounded grab ([grabFocusUntilLanded]) that returns focus
 * to the focused pane when the grid becomes the active layer again. A
 * fresh pane can be attached but not yet placed when that grab fires, and
 * a raw requestFocus there crashes in the focus system's bring-into-view
 * coroutine — the placement gate holds the request back.
 */
internal class MultiviewPaneFocus {
    private val placed = mutableStateOf(emptySet<Int>())
    private val owner = mutableStateOf<Int?>(null)

    fun onPanePlaced(id: Int) {
        placed.value = placed.value + id
    }

    fun onPaneFocusChanged(
        id: Int,
        hasFocus: Boolean,
    ) {
        when {
            hasFocus -> owner.value = id
            owner.value == id -> owner.value = null
        }
    }

    /** Drops placement facts for removed panes (ids can be recycled). */
    fun prune(liveIds: List<Int>) {
        placed.value = placed.value.filterTo(mutableSetOf()) { it in liveIds }
    }

    /** Re-lands focus on pane [id]: placement-gated, bounded, sticky. */
    suspend fun grabOnto(
        id: Int,
        request: () -> Unit,
        awaitFrame: suspend () -> Unit,
    ) {
        grabFocusUntilLanded(
            landed = { owner.value == id },
            request = request,
            awaitFrame = awaitFrame,
            placed = { id in placed.value },
        )
    }
}
