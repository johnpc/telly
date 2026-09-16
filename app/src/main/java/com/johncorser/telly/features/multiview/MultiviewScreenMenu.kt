package com.johncorser.telly.features.multiview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.johncorser.telly.core.design.TELLY_MENU_SHEET
import com.johncorser.telly.core.ui.FocusScreenDefaults
import com.johncorser.telly.core.ui.TellyScreenMenuRow
import com.johncorser.telly.core.ui.rememberFocusSeed

/**
 * The pane menu (multiview-round 04): a 200 dp dark popup right of the
 * focused pane, vertically centered on it, 40 dp rows in captured order —
 * Add screen / Search and add / Change channel (+ telly's Remove screen
 * while the grid has more than one pane). Focus starts on the first row.
 */
@Composable
internal fun MultiviewScreenMenu(
    viewModel: MultiviewViewModel,
    panes: List<MultiviewPane>,
    focusedId: Int,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val rows = viewModel.menuRows()
        val index = panes.indexOfFirst { it.id == focusedId }.coerceAtLeast(0)
        val cell = MultiviewGrid.cells(panes.size)[index]
        val (x, y) = MultiviewGrid.menuOffset(cell, maxWidth.value, maxHeight.value, rows.size)
        val seed = rememberFocusSeed()
        Column(
            Modifier
                .offset(x = x.dp, y = y.dp)
                .width(MultiviewGrid.MENU_WIDTH_DP.dp)
                .background(Color(TELLY_MENU_SHEET), RoundedCornerShape(FocusScreenDefaults.cornerRadius))
                .then(seed.modifier()),
        ) {
            rows.forEachIndexed { rowIndex, action ->
                TellyScreenMenuRow(
                    label = action.label,
                    onClick = { viewModel.onMenuAction(action) },
                    height = MultiviewGrid.MENU_ROW_DP.dp,
                    fontSize = 16.sp,
                    requestFocus = rowIndex == 0,
                    grabYielded = seed.seeded,
                )
            }
        }
    }
}
