package com.johncorser.telly.features.guide

/**
 * Round-7 P2: the reference does NOT dim the row the context sheet was
 * opened on — screencap probes show the long-OK row's pixels identical
 * rest vs sheet-open (full brightness, white focus outline intact, the
 * whole [0,1920] row band) while every other row multiplies by the
 * scrim's 0.40. The guide host punches this band out of the sheet scrim.
 */
object GuideDimExemption {
    /**
     * Top of the undimmed full-width row band in dp from the screen top,
     * or null when there is nothing to exempt: no saved channel, the
     * channel left the rows (hidden under its own sheet), or its current
     * row sits outside the grid viewport — then the uniform scrim stands.
     * The band tracks the channel's CURRENT index because rows can
     * re-emit under the open sheet (the [GuideFocusMemory] rationale).
     */
    fun bandTopDp(
        channelId: Long?,
        rows: List<GuideRow>,
        firstRow: Int,
    ): Float? {
        val id = channelId ?: return null
        val rowIndex = rows.indexOfFirst { it.channel.id == id }
        val offsetDp = (rowIndex - firstRow) * GuideGeometry.ROW_HEIGHT_DP
        return (GuideGeometry.GRID_TOP_DP + offsetDp)
            .takeIf { rowIndex >= firstRow && offsetDp < GuideGeometry.GRID_HEIGHT_DP }
    }
}
