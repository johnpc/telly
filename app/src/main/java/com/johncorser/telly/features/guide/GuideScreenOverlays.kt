package com.johncorser.telly.features.guide

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.johncorser.telly.features.panel.ChannelPanelScreenGroups
import com.johncorser.telly.features.settings.SettingsScreenPaywall

/** Groups column slid in at the guide's left (capture 25), grid shifts right. */
@Composable
internal fun GuideScreenGroups(controller: GuideController) {
    val groups by controller.groups.collectAsState()
    val selected by controller.selectedGroup.collectAsState()
    Box(Modifier.fillMaxHeight()) {
        ChannelPanelScreenGroups(
            groups = groups,
            selected = selected,
            onSelect = controller::selectGroup,
            autoFocusSelected = true,
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
