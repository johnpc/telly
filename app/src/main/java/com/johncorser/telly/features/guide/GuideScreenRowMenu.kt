package com.johncorser.telly.features.guide

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.johncorser.telly.R
import com.johncorser.telly.core.ui.OnboardingScreenMessage
import com.johncorser.telly.features.mylist.MyListKeys
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
    val myListKeys by controller.myList.keys.collectAsState()
    val row = focus?.let { rows.getOrNull(it.rowIndex) } ?: return
    PlaybackScreenMenu(
        sections = PlayerMenu.sections(info?.title, row.channel.source.name),
        favorite = row.channel.flags.favorite,
        onItem = controller.menu::onMenuItem,
        restore = controller.menu.sheetFocus.restore,
        inMyList = MyListKeys.saved(myListKeys, row.channel, focus?.cell?.program?.startMs),
    )
}

/**
 * Screens the sheet routes to. Description and coming-soon are pushed —
 * BACK pops one level back to the sheet; Channel options REPLACES the
 * sheet, so its BACK lands directly on the grid (ref-round6 §A).
 */
@Composable
internal fun GuideScreenRowMenuLayers(
    controller: GuideController,
    layer: GuideLayer,
) {
    when (layer) {
        is GuideLayer.ChannelOptions ->
            GuideScreenChannelOptionsPane(layer.channelName, controller.menu::onChannelOption)
        is GuideLayer.Description -> OnboardingScreenMessage(headline = layer.title, subtitle = layer.text)
        is GuideLayer.ComingSoon ->
            OnboardingScreenMessage(
                headline = layer.feature,
                subtitle = stringResource(R.string.playback_coming_soon),
            )
        else -> Unit
    }
}

/**
 * The §41 "Channel options" pane (captures 41-42): a settings-shell sheet
 * titled with the channel name, every row locked. Shared verbatim with the
 * playback panel's sheet.
 */
@Composable
internal fun GuideScreenChannelOptionsPane(
    channelName: String,
    onActivate: (String) -> Unit,
) {
    SettingsScreenSheet(title = channelName) {
        SettingsScreenRows(
            rows = GuideChannelOptions.rows(channelName),
            onActivate = onActivate,
        )
    }
}
