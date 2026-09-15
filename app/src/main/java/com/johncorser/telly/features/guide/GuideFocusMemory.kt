package com.johncorser.telly.features.guide

/** One settable unit of engine state: focus (null = none) + scroll. */
data class GuideFocusState(
    val focus: GuideFocus?,
    val scrollX: Float,
    val firstRow: Int,
)

/** Identity-keyed snapshot of the grid's focus + scroll behind a sheet. */
data class GuideFocusSnapshot(
    val channelId: Long,
    val state: GuideFocusState,
)

/**
 * Saves the grid's focus when the row context sheet opens and re-asserts
 * it when the layer chain closes back to the grid. The engine's focus is
 * positional (row INDEX), so rows re-emitting under an open sheet
 * (favorite/hide from the sheet, a playlist or EPG refresh) would
 * otherwise land it on whatever channel slid into that index. The
 * snapshot is keyed by channel id and consumed on the first restore; a
 * vanished channel (hidden from its own sheet) keeps the engine's
 * index-resolved fallback.
 */
class GuideFocusMemory(
    private val engine: GuideFocusEngine,
    private val visibleRows: () -> Int = { GuideGeometry.VISIBLE_ROWS },
    private val rows: () -> List<GuideRow>,
) {
    private var snapshot: GuideFocusSnapshot? = null
    private var lastChannelId: Long? = null

    /**
     * The channel the sheet was last opened on. Unlike the positional
     * snapshot it survives the restore, so the scrim's undimmed-row hole
     * (round7 P2) can outlive the sheet through the scrim's fade-out.
     */
    val savedChannelId: Long? get() = lastChannelId

    /** Snapshots the focused channel, cell and scroll as the sheet opens. */
    fun save() {
        val focus = engine.focus.value
        val channel = focus?.let { rows().getOrNull(it.rowIndex)?.channel }
        lastChannelId = channel?.id
        snapshot =
            channel?.let {
                GuideFocusSnapshot(it.id, GuideFocusState(focus, engine.scrollX.value, engine.firstVisibleRow.value))
            }
    }

    /** Re-asserts the snapshot once, on the first return to the grid. */
    fun restore() {
        val saved = snapshot ?: return
        snapshot = null
        val focus = saved.state.focus ?: return
        val list = rows()
        val rowIndex = list.indexOfFirst { it.channel.id == saved.channelId }
        val cell = rowIndex.takeIf { it >= 0 }?.let { cellFor(list[it].cells, focus) } ?: return
        val firstRow =
            GuideScrollPlanner.rowWindow(saved.state.firstRow, rowIndex, visibleRows(), list.size)
        engine.apply(saved.state.copy(focus = GuideFocus(rowIndex, cell, focus.anchorMs), firstRow = firstRow))
    }

    /** The saved cell if it still exists, else the cell at the anchor. */
    private fun cellFor(
        cells: List<GuideCell>,
        saved: GuideFocus,
    ): GuideCell? =
        cells.firstOrNull { it.startMs == saved.cell.startMs }
            ?: GuideFocusNav.cellAt(cells, saved.anchorMs)
}
