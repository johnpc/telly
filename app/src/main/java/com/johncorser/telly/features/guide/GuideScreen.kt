package com.johncorser.telly.features.guide

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.johncorser.telly.core.design.TELLY_ONBOARDING_BACKGROUND
import com.johncorser.telly.core.ui.ScreenLifecycleStartStop
import com.johncorser.telly.core.ui.TellyScreenKeyAnchor
import com.johncorser.telly.features.playback.PlaybackScreenBlockGate
import com.johncorser.telly.features.playback.PlaybackScreenMenuSurfaceSwitch
import com.johncorser.telly.features.playback.PlayerMenuSurface

/**
 * The TV guide (capture 24): preview window + info pane on top, the
 * virtualized programme grid below. The grid layer routes D-pad keys
 * through the controller's pure engine; overlaid layers (groups column,
 * cell dropdown, coming-soon) use regular Compose focus. BACK on the grid is
 * deliberately unhandled: at guide root the app exits, the
 * device-verified free-tier behavior.
 */
@Composable
fun GuideScreen(
    deps: GuideDeps,
    onFullscreen: () -> Unit,
    onOpenSearch: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenVod: () -> Unit = {},
    onOpenRecordings: () -> Unit = {},
    settingsOpen: Boolean = false,
    onOpenMyList: () -> Unit = {},
    onOpenManageFavorites: () -> Unit = {},
    onOpenReorderChannels: (String) -> Unit = {},
    onOpenNamesEditor: () -> Unit = {},
) {
    val engine = remember { deps.playback.engineFactory() }
    val callbacks =
        remember {
            GuideCallbacks(
                onFullscreen = onFullscreen,
                onOpenSearch = onOpenSearch,
                onOpenSettings = onOpenSettings,
                onOpenManageFavorites = onOpenManageFavorites,
                onOpenReorderChannels = onOpenReorderChannels,
                onOpenNamesEditor = onOpenNamesEditor,
                external = deps.playback.hooks.platform.external,
            )
        }
    val controller = rememberGuideController(deps, engine, callbacks)
    val detectors = remember { GuideScreenKeyDetectors() }
    val layer by controller.layer.collectAsState()
    val blockPrompt by controller.blockPrompt.channel.collectAsState()
    // Background/resume: stop the preview stream on STOP, re-seed the clock
    // and re-tune on the START after it (round7 resume P2).
    ScreenLifecycleStartStop(onStart = controller.lifecycle::onForeground, onStop = controller.lifecycle::onBackground)
    BackHandler(enabled = layer != GuideLayer.Grid && !settingsOpen) { controller.onKey(GuideKey.BACK) }
    // Returning from the settings sheet lands back on the grid, whose key
    // anchor re-grabs focus when it recomposes.
    LaunchedEffect(settingsOpen) { if (!settingsOpen) controller.menu.reset() }
    Box(
        Modifier
            .fillMaxSize()
            // Appearance -> TV guide -> Panel transparency (Opaque = today).
            .background(Color(TELLY_ONBOARDING_BACKGROUND).copy(alpha = LocalGuideStyle.current.backgroundAlpha)),
    ) {
        Row(Modifier.fillMaxSize()) {
            if (layer == GuideLayer.Groups) {
                GuideScreenGroups(controller, onOpenSearch, onOpenSettings, onOpenMyList, onOpenVod, onOpenRecordings)
            }
            Box(Modifier.weight(1f)) {
                Column(Modifier.fillMaxSize()) {
                    GuideScreenTop(controller, engine)
                    GuideScreenHeader(controller)
                    GuideScreenGrid(controller, dimFocus = layer == GuideLayer.Groups, Modifier.weight(1f))
                }
                if (layer == GuideLayer.Grid && blockPrompt == null) {
                    TellyScreenKeyAnchor { event -> mapGuideKey(event, detectors)?.let(controller::onKey) ?: false }
                }
                GuideScreenCellMenu(controller, layer)
            }
        }
        GuideScreenHintToast(controller, Modifier.align(Alignment.BottomEnd))
        // The dim behind the sheet lives outside the layer switch so it can
        // fade back out (~300 ms) after the sheet's instant cut (ref-round6);
        // the originating row is punched out undimmed (round7 P2).
        GuideScreenMenuScrim(controller, layer)
        // Channel options cross-fades in place of the sheet on push and
        // fades out on pop straight to the grid (ref-round6 §A).
        PlaybackScreenMenuSurfaceSwitch(layer, ::guideMenuSurface) { menuLayer ->
            GuideScreenRowMenu(controller, menuLayer)
            GuideScreenRowMenuLayers(controller, menuLayer)
        }
        // Tuning a blocked channel (grid OK / preview restore) gates on the PIN.
        if (blockPrompt != null) {
            PlaybackScreenBlockGate(
                onSubmit = controller.blockPrompt::submit,
                onDismiss = controller.blockPrompt::dismiss,
            )
        }
    }
}

private fun guideMenuSurface(layer: GuideLayer): PlayerMenuSurface =
    when (layer) {
        GuideLayer.RowMenu -> PlayerMenuSurface.SHEET
        is GuideLayer.ChannelOptions -> PlayerMenuSurface.CHANNEL_OPTIONS
        is GuideLayer.Description, is GuideLayer.ComingSoon, is GuideLayer.BlockPin -> PlayerMenuSurface.PUSHED
        is GuideLayer.RecordingStop, is GuideLayer.CustomRecording -> PlayerMenuSurface.PUSHED
        else -> PlayerMenuSurface.NONE
    }
