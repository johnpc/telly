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
import androidx.compose.ui.unit.dp
import com.johncorser.telly.core.design.TELLY_BUTTON_RESTING
import com.johncorser.telly.core.ui.TellyScreenMenuRow

/**
 * Groups column (capture 25): Favorites, All channels, then playlist groups.
 * The selected group keeps a dark pill; focus is the usual white pill. The
 * guide overlay asks the selected row to grab focus when the column opens.
 */
@Composable
internal fun ChannelPanelScreenGroups(
    groups: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    autoFocusSelected: Boolean = false,
) {
    LazyColumn(
        Modifier
            .width(220.dp)
            .fillMaxHeight()
            .padding(start = 16.dp, top = 40.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(groups, key = { it }) { group ->
            TellyScreenMenuRow(
                label = group,
                onClick = { onSelect(group) },
                height = 39.dp,
                restingContainer = if (group == selected) Color(TELLY_BUTTON_RESTING) else Color.Transparent,
                requestFocus = autoFocusSelected && group == selected,
            )
        }
    }
}
