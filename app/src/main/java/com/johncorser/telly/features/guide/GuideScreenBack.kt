package com.johncorser.telly.features.guide

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * The guide root's BACK chain (director round: BACK mirrors LEFT). BACK on the
 * bare grid opens the groups column, and every overlaid layer closes one level
 * at a time — both routed through the layer policy here. The remaining legs
 * (groups column → settings gear → exit) are Compose-spatial focus and live in
 * [GuideScreenGroups]; that layer owns its own BACK, so it is excluded here so
 * its handler wins.
 */
@Composable
internal fun GuideScreenBackHandlers(
    controller: GuideController,
    layer: GuideLayer,
    settingsOpen: Boolean,
) {
    BackHandler(enabled = layer != GuideLayer.Groups && !settingsOpen) { controller.onKey(GuideKey.BACK) }
}

/**
 * The last leg of the LEFT-mirroring chain: BACK on the settings gear exits
 * the app, gated by "Confirm exit by second press Back" read at press time.
 */
@Composable
internal fun rememberGuideExit(
    deps: GuideDeps,
    controller: GuideController,
): () -> Unit {
    val activity = LocalContext.current as? Activity
    return { if (!deps.start.confirmExit() || controller.chrome.exit.onBack()) activity?.finish() }
}
