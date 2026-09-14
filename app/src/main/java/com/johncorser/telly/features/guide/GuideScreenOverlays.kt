package com.johncorser.telly.features.guide

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.johncorser.telly.features.panel.ChannelPanelScreenGroups
import com.johncorser.telly.features.panel.GroupColumnMetrics
import com.johncorser.telly.features.settings.SettingsScreenPaywall

/**
 * Nav rail + groups column slid in at the guide's left (capture 25), grid
 * shifts right. The rail is the nav slice's visual placeholder: 56 dp,
 * decorative only, focus stays in the groups list.
 */
@Composable
internal fun GuideScreenGroups(controller: GuideController) {
    val groups by controller.groups.collectAsState()
    val selected by controller.selectedGroup.collectAsState()
    Row(Modifier.fillMaxHeight()) {
        GuideScreenRail()
        ChannelPanelScreenGroups(
            groups = groups,
            selected = selected,
            onSelect = controller::selectGroup,
            autoFocusSelected = true,
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

/** Every dropdown row lands on the shared Unlock Premium screen. */
@Composable
internal fun GuideScreenPaywallLayer(
    controller: GuideController,
    layer: GuideLayer,
) {
    if (layer !is GuideLayer.Paywall) return
    Box(Modifier.fillMaxSize()) {
        SettingsScreenPaywall(onClose = controller::closeLayer)
    }
}
