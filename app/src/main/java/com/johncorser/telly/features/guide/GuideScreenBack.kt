package com.johncorser.telly.features.guide

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * The guide root's BACK chain: overlaid layers close one at a time; BACK on
 * the bare grid exits the app (the device-verified free-tier behavior),
 * gated by "Confirm exit by second press Back" when that toggle is on. The
 * toggle is read at press time so a fresh change applies immediately: OFF
 * (default) finishes on the first BACK — exactly the exit-with-no-
 * confirmation the e2e suite pins — while ON warns first and exits on a
 * second BACK inside the window.
 */
@Composable
internal fun GuideScreenBackHandlers(
    deps: GuideDeps,
    controller: GuideController,
    layer: GuideLayer,
    settingsOpen: Boolean,
) {
    BackHandler(enabled = layer != GuideLayer.Grid && !settingsOpen) { controller.onKey(GuideKey.BACK) }
    val activity = LocalContext.current as? Activity
    BackHandler(enabled = layer == GuideLayer.Grid && !settingsOpen) {
        if (!deps.start.confirmExit() || controller.chrome.exit.onBack()) activity?.finish()
    }
}
