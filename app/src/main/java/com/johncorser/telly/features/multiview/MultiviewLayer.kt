package com.johncorser.telly.features.multiview

/** Which layer covers the multiview grid (spec: panes -> menu -> picker). */
sealed interface MultiviewLayer {
    /** The bare pane grid (single pane = the captured teaser framing). */
    data object Panes : MultiviewLayer

    /** OK on a pane: Add screen / Search and add / Change channel [/ Remove]. */
    data object Menu : MultiviewLayer

    /** The channel picker every menu row opens (multiview-round 05/08/10). */
    data class Picker(
        val mode: MultiviewPickerMode,
    ) : MultiviewLayer
}

/** What a picker selection does: fill the next pane or retune the focused one. */
enum class MultiviewPickerMode { ADD, CHANGE }

/** Pane-menu rows, captured order (multiview-round 04) + telly's Remove. */
enum class MultiviewMenuAction(
    val label: String,
) {
    ADD_SCREEN("Add screen"),
    SEARCH_AND_ADD("Search and add"),
    CHANGE_CHANNEL("Change channel"),
    REMOVE_SCREEN("Remove screen"),
}

/**
 * Remove screen only exists while the grid has panes to spare (>1), and the
 * two add rows leave at the [MultiviewGrid.MAX_PANES] cap (round8 :44-46 —
 * listing a no-op "Add screen" at four panes was the logged delta).
 */
object MultiviewMenu {
    fun rows(paneCount: Int): List<MultiviewMenuAction> =
        MultiviewMenuAction.entries.filter { action ->
            when (action) {
                MultiviewMenuAction.ADD_SCREEN, MultiviewMenuAction.SEARCH_AND_ADD ->
                    paneCount < MultiviewGrid.MAX_PANES
                MultiviewMenuAction.CHANGE_CHANNEL -> true
                MultiviewMenuAction.REMOVE_SCREEN -> paneCount > 1
            }
        }
}
