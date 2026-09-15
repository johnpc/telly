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

/** Remove screen only exists while the grid has panes to spare (>1). */
object MultiviewMenu {
    fun rows(paneCount: Int): List<MultiviewMenuAction> =
        if (paneCount > 1) {
            MultiviewMenuAction.entries
        } else {
            MultiviewMenuAction.entries - MultiviewMenuAction.REMOVE_SCREEN
        }
}
