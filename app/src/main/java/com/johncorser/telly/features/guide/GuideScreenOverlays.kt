package com.johncorser.telly.features.guide

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.johncorser.telly.features.panel.ChannelPanelScreenGroups
import com.johncorser.telly.features.panel.GroupColumnMetrics

/**
 * Nav rail + groups column slid in at the guide's left (capture 25), grid
 * shifts right. The rail is the nav slice's visual placeholder: 56 dp,
 * decorative only, focus stays in the groups list.
 */
@Composable
internal fun GuideScreenGroups(
    controller: GuideController,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val groups by controller.groups.collectAsState()
    val selected by controller.selectedGroup.collectAsState()
    val searchFocus = remember { FocusRequester() }
    val gearFocus = remember { FocusRequester() }
    val groupsFocus = remember { FocusRequester() }
    Row(Modifier.fillMaxHeight()) {
        GuideScreenRail(onOpenSearch, onOpenSettings, searchFocus, gearFocus, groupsFocus)
        // RIGHT leaves the column back to the grid (capture 25); the grid
        // has no focusables for the focus search to find, so the key is
        // routed to the layer policy before it dead-ends.
        Box(
            Modifier
                .focusRequester(groupsFocus)
                .onPreviewKeyEvent { event ->
                    event.type == KeyEventType.KeyDown &&
                        event.key == Key.DirectionRight &&
                        controller.onKey(GuideKey.RIGHT)
                },
        ) {
            ChannelPanelScreenGroups(
                groups = groups,
                selected = selected,
                onSelect = controller::selectGroup,
                autoFocusSelected = true,
                rowModifier = Modifier.focusProperties { left = gearFocus },
                metrics =
                    GroupColumnMetrics(
                        width = 252.dp,
                        topPadding = 213.dp,
                        rowHeight = 36.dp,
                        rowSpacing = 2.5.dp,
                        fontSize = 18.sp,
                    ),
            )
        }
    }
}
