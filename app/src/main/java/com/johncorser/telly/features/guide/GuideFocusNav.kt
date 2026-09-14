package com.johncorser.telly.features.guide

/**
 * Pure focus-movement semantics inside one row's cell strip. LEFT stops at
 * the cell flush with the window's left edge (from where LEFT opens the
 * groups column, capture 25); UP/DOWN pick the cell at a constant time
 * anchor, TiviMate's keep-the-column behavior.
 */
object GuideFocusNav {
    fun rightOf(
        cells: List<GuideCell>,
        focused: GuideCell,
    ): GuideCell? {
        val index = indexOf(cells, focused)
        if (index < 0) return null
        return cells.getOrNull(index + 1)
    }

    /**
     * The previous cell, or null when focus already touches the window's
     * left edge — the caller opens the groups column then.
     */
    fun leftOf(
        cells: List<GuideCell>,
        focused: GuideCell,
        windowStartMs: Long,
    ): GuideCell? {
        if (focused.startMs <= windowStartMs) return null
        val index = indexOf(cells, focused)
        if (index < 0) return null
        return cells.getOrNull(index - 1)
    }

    /** The cell containing [anchorMs], else the nearest edge cell. */
    fun cellAt(
        cells: List<GuideCell>,
        anchorMs: Long,
    ): GuideCell? {
        val containing = cells.firstOrNull { it.contains(anchorMs) }
        val first = cells.firstOrNull()
        return containing ?: first?.takeIf { anchorMs < it.startMs } ?: cells.lastOrNull()
    }

    /** First load: the first row's airing cell, anchored at now (uidump 24). */
    fun initialFocus(
        rows: List<GuideRow>,
        nowMs: Long,
    ): GuideFocus? =
        rows
            .firstOrNull()
            ?.let { cellAt(it.cells, nowMs) }
            ?.let { GuideFocus(rowIndex = 0, cell = it, anchorMs = nowMs) }

    private fun indexOf(
        cells: List<GuideCell>,
        focused: GuideCell,
    ): Int = cells.indexOfFirst { it.startMs == focused.startMs }
}
