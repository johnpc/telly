package com.johncorser.telly.features.guide

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.johncorser.telly.R
import com.johncorser.telly.core.ui.OnboardingScreenMessage
import com.johncorser.telly.features.playback.PlaybackScreenMenu
import com.johncorser.telly.features.playback.PlayerMenu
import com.johncorser.telly.features.settings.SettingsScreenRows
import com.johncorser.telly.features.settings.SettingsScreenSheet

/**
 * Long-OK/MENU on a guide row: the full right-side context sheet with the
 * grid still visible behind it (round3-ref 05, captures 38-40). The sheet
 * itself is the shared playback-panel one (256 dp wide, 8 dp off the
 * screen's top/right, 40 dp row pitch, pill inset 8 dp — 2 px = 1 dp).
 */
@Composable
internal fun GuideScreenRowMenu(
    controller: GuideController,
    layer: GuideLayer,
) {
    if (layer !is GuideLayer.RowMenu) return
    val rows by controller.rows.collectAsState()
    val focus by controller.focus.collectAsState()
    val info by controller.info.collectAsState()
    val row = focus?.let { rows.getOrNull(it.rowIndex) } ?: return
    PlaybackScreenMenu(
        sections = PlayerMenu.sections(info?.title, row.channel.source.name),
        favorite = row.channel.flags.favorite,
        onItem = controller.menu::onMenuItem,
    )
}

/** Screens the sheet pushes; BACK pops one level back to the sheet. */
@Composable
internal fun GuideScreenRowMenuLayers(
    controller: GuideController,
    layer: GuideLayer,
) {
    when (layer) {
        is GuideLayer.ChannelOptions ->
            SettingsScreenSheet(title = layer.channelName) {
                SettingsScreenRows(
                    rows = GuideChannelOptions.rows(layer.channelName),
                    onActivate = controller.menu::onChannelOption,
                )
            }
        is GuideLayer.Description -> OnboardingScreenMessage(headline = layer.title, subtitle = layer.text)
        is GuideLayer.ComingSoon ->
            OnboardingScreenMessage(
                headline = layer.feature,
                subtitle = stringResource(R.string.playback_coming_soon),
            )
        else -> Unit
    }
}
