package com.johncorser.telly.features.panel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.johncorser.telly.core.design.TELLY_BUTTON_RESTING
import com.johncorser.telly.core.ui.TellyScreenMenuRow
import com.johncorser.telly.core.ui.rememberFocusSeed

/**
 * Groups column (captures 25/47): Favorites, All channels, then playlist
 * groups. The selected group keeps a dark pill; focus is the usual white
 * pill. The guide overlay asks the selected row to grab focus when the
 * column opens and passes the guide's own metrics (252 dp panel, 36 dp
 * pills at a 38.5 dp pitch, 18 sp labels, list top under the preview
 * line — uidump 25); the playback panel keeps the defaults.
 */
@Composable
internal fun ChannelPanelScreenGroups(
    groups: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    autoFocusSelected: Boolean = false,
    metrics: GroupColumnMetrics = GroupColumnMetrics(),
    rowModifier: Modifier = Modifier,
) {
    val seed = rememberFocusSeed()
    LazyColumn(
        Modifier
            .width(metrics.width)
            .fillMaxHeight()
            .padding(start = 16.dp, top = metrics.topPadding, end = 16.dp)
            .then(seed.modifier()),
        verticalArrangement = Arrangement.spacedBy(metrics.rowSpacing),
    ) {
        items(groups, key = { it }) { group ->
            val isSelected = group == selected
            TellyScreenMenuRow(
                label = group,
                onClick = { onSelect(group) },
                modifier = rowModifier.semantics { this.selected = isSelected },
                height = metrics.rowHeight,
                fontSize = metrics.fontSize,
                restingContainer = if (isSelected) Color(TELLY_BUTTON_RESTING) else Color.Transparent,
                requestFocus = autoFocusSelected && isSelected,
                grabYielded = seed.seeded,
            )
        }
    }
}

/** Geometry knobs for the groups column; defaults = the playback panel. */
data class GroupColumnMetrics(
    val width: Dp = 220.dp,
    val topPadding: Dp = 40.dp,
    val rowHeight: Dp = 39.dp,
    val rowSpacing: Dp = 4.dp,
    val fontSize: TextUnit = 15.sp,
)
