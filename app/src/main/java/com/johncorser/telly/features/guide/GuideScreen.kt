package com.johncorser.telly.features.guide

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.johncorser.telly.core.design.TELLY_ONBOARDING_BACKGROUND
import com.johncorser.telly.core.ui.TellyScreenKeyAnchor

/**
 * The TV guide (capture 24): preview window + info pane on top, the
 * virtualized programme grid below. The grid layer routes D-pad keys
 * through the controller's pure engine; overlaid layers (groups column,
 * cell dropdown, paywall) use regular Compose focus. BACK on the grid is
 * deliberately unhandled: at guide root the app exits, the
 * device-verified free-tier behavior.
 */
@Composable
fun GuideScreen(
    deps: GuideDeps,
    onFullscreen: () -> Unit,
) {
    val engine = remember { deps.playback.engineFactory() }
    val controller = rememberGuideController(deps, engine, onFullscreen)
    val detectors = remember { GuideScreenKeyDetectors() }
    val layer by controller.layer.collectAsState()
    BackHandler(enabled = layer != GuideLayer.Grid) { controller.onKey(GuideKey.BACK) }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(TELLY_ONBOARDING_BACKGROUND)),
    ) {
        Row(Modifier.fillMaxSize()) {
            if (layer == GuideLayer.Groups) GuideScreenGroups(controller)
            Box(Modifier.weight(1f)) {
                Column(Modifier.fillMaxSize()) {
                    GuideScreenTop(controller, engine)
                    GuideScreenHeader(controller)
                    GuideScreenGrid(controller, dimFocus = layer == GuideLayer.Groups, Modifier.weight(1f))
                }
                if (layer == GuideLayer.Grid) {
                    TellyScreenKeyAnchor { event -> mapGuideKey(event, detectors)?.let(controller::onKey) ?: false }
                }
                GuideScreenCellMenu(controller, layer)
            }
        }
        GuideScreenHintToast(controller, Modifier.align(Alignment.BottomEnd))
        GuideScreenPaywallLayer(controller, layer)
    }
}
