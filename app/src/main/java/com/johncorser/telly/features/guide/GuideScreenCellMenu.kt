package com.johncorser.telly.features.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.johncorser.telly.core.design.TELLY_MENU_SHEET
import com.johncorser.telly.core.ui.TellyScreenMenuRow

/**
 * The future-cell dropdown, anchored under the focused cell (capture 27):
 * Remind / Record / Custom recording / Add to My list / Program
 * description — all premium-gated in free (capture 31).
 */
@Composable
internal fun GuideScreenCellMenu(
    controller: GuideController,
    layer: GuideLayer,
) {
    if (layer !is GuideLayer.CellMenu) return
    val focus by controller.focus.collectAsState()
    val firstRow by controller.firstVisibleRow.collectAsState()
    val scrollX by controller.scrollX.collectAsState()
    val focused = focus ?: return
    val anchor =
        GuideDropdownAnchor.position(
            focus = focused,
            firstVisibleRow = firstRow,
            originMs = controller.originMs,
            scrollXDp = scrollX,
        )
    Column(
        Modifier
            .padding(top = GuideGeometry.GRID_TOP_DP.dp)
            .offset(x = anchor.xDp.dp, y = anchor.yDp.dp)
            .width(GuideDropdownAnchor.MENU_WIDTH_DP.dp)
            .background(Color(TELLY_MENU_SHEET), RoundedCornerShape(4.dp)),
    ) {
        GuideCellAction.entries.forEachIndexed { index, action ->
            TellyScreenMenuRow(
                label = action.label,
                onClick = { controller.onCellAction(action) },
                height = GuideDropdownAnchor.MENU_ROW_DP.dp,
                requestFocus = index == 0,
            )
        }
    }
}
