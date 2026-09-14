package com.johncorser.telly.features.guide

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.johncorser.telly.core.design.TELLY_ONBOARDING_BACKGROUND
import com.johncorser.telly.features.playback.PlaybackEnv

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
    val scope = rememberCoroutineScope()
    val engine = remember { deps.playback.engineFactory() }
    val controller =
        remember {
            GuideController(
                env =
                    PlaybackEnv(
                        channelDao = deps.playback.channelDao,
                        epgRepository = deps.playback.epgRepository,
                        engine = engine,
                        store = deps.playback.keyValueStore,
                        clock = deps.playback.clock,
                    ),
                pastDays = deps.pastDays,
                scope = scope,
                onFullscreen = onFullscreen,
            )
        }
    DisposableEffect(Unit) { onDispose { controller.close() } }
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
                    GuideScreenGrid(controller, Modifier.weight(1f))
                }
                if (layer == GuideLayer.Grid) GuideScreenKeyAnchor(controller::onKey)
                GuideScreenCellMenu(controller, layer)
            }
        }
        GuideScreenPaywallLayer(controller, layer)
    }
}
