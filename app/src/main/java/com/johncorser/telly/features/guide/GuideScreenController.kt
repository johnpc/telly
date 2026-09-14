package com.johncorser.telly.features.guide

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.johncorser.telly.features.history.HistoryGroup
import com.johncorser.telly.features.panel.PanelViewModel
import com.johncorser.telly.features.playback.PlaybackEnv
import com.johncorser.telly.features.player.Media3PlayerEngine

/** Builds the guide's controller over [engine]; closes it on dispose. */
@Composable
internal fun rememberGuideController(
    deps: GuideDeps,
    engine: Media3PlayerEngine,
    onFullscreen: () -> Unit,
    historySource: Boolean = false,
): GuideController {
    val scope = rememberCoroutineScope()
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
                history = deps.playback.history,
                pastDays = deps.pastDays,
                scope = scope,
                onFullscreen = onFullscreen,
                initialGroup = if (historySource) HistoryGroup.NAME else PanelViewModel.ALL_CHANNELS,
            )
        }
    DisposableEffect(Unit) { onDispose { controller.close() } }
    return controller
}
