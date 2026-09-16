package com.johncorser.telly.features.guide

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp

/**
 * One 39 dp grid row (uidump 24, 78 px pitch): number, 45×30 dp logo tile
 * and name in the fixed channel column, then the cell strip clipped to the
 * time viewport. The previewed channel's number/name go accent blue with a
 * ▶ marker right-aligned at the column edge (capture 32). [dimFocus] drops
 * the focused cell to the grey "selected" pill while the groups column
 * owns the white focus (capture 25).
 */
@Composable
internal fun GuideScreenRow(
    row: GuideRow,
    focusedCell: GuideCell?,
    playing: Boolean,
    dimFocus: Boolean,
    scrollXDp: Float,
    originMs: Long,
    nowMs: Long,
) {
    Row(
        Modifier
            .fillMaxWidth()
            // Appearance -> TV guide -> Number of visible channels (7 = today's 39 dp pitch).
            .height(LocalGuideStyle.current.rowHeightDp.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GuideScreenChannelColumn(row, playing)
        Box(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .clipToBounds(),
        ) {
            row.cells.forEach { cell ->
                GuideCellLayout.place(cell, originMs, scrollXDp, GuideGeometry.TIME_VIEWPORT_DP)?.let { placement ->
                    GuideScreenCell(
                        cell = cell,
                        placement = placement,
                        focused = cell.startMs == focusedCell?.startMs,
                        dimFocus = dimFocus,
                        past = cell.endMs <= nowMs,
                    )
                }
            }
        }
    }
}
