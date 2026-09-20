package com.johncorser.telly.features.guide

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.johncorser.telly.features.panel.ChannelPanelScreenGroups
import com.johncorser.telly.features.panel.GroupColumnMetrics
import com.johncorser.telly.features.panel.LocalGroupVisibility

/**
 * Nav rail + groups column slid in at the guide's left (capture 25), grid
 * shifts right. The rail's search, bookmark (My List), Movies film and
 * gear icons are live targets; focus otherwise stays in the groups list.
 * BACK continues the director's LEFT-mirroring chain: from the groups column
 * it moves to the settings gear, and from the rail it [onExit]s the app.
 */
@Composable
internal fun GuideScreenGroups(
    controller: GuideController,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenMyList: () -> Unit = {},
    onOpenVod: () -> Unit = {},
    onOpenRecordings: () -> Unit = {},
    onExit: () -> Unit = {},
) {
    val groups by controller.groups.collectAsState()
    val selected by controller.selectedGroup.collectAsState()
    val searchFocus = remember { FocusRequester() }
    val gearFocus = remember { FocusRequester() }
    val groupsFocus = remember { FocusRequester() }
    var railFocused by remember { mutableStateOf(false) }
    // On the rail (e.g. the gear) BACK exits; on the groups column it moves to
    // the gear — the same landing as one more LEFT.
    BackHandler { if (railFocused) onExit() else gearFocus.requestFocus() }
    Row(Modifier.fillMaxHeight()) {
        Box(Modifier.onFocusEvent { railFocused = it.hasFocus }) {
            GuideScreenRail(
                onOpenSearch = onOpenSearch,
                onOpenSettings = onOpenSettings,
                onOpenMyList = onOpenMyList,
                onOpenVod = onOpenVod,
                onOpenRecordings = onOpenRecordings,
                searchFocus = searchFocus,
                gearFocus = gearFocus,
                groupsFocus = groupsFocus,
            )
        }
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
                // Appearance -> Groups: hidden synthetic groups drop out.
                groups = LocalGroupVisibility.current.filter(groups),
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
