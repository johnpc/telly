package com.johncorser.telly.features.guide

import com.johncorser.telly.features.playback.ChannelActions
import com.johncorser.telly.features.playback.PlayerMenuItem
import com.johncorser.telly.features.playback.PlayerMenuRoute
import com.johncorser.telly.features.playback.PlayerMenuRouting
import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.settings.RowIds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The guide's overlay-layer state machine plus the long-OK row context
 * sheet (catalogue §3 38-42 + round3-ref 05), routed through the shared
 * [PlayerMenuRouting] table: genuinely supported rows act (Search,
 * Settings, favorites toggle, hide, the programme description); rows the
 * free reference locks behind Premium open the shared Unlock Premium
 * screen; the remaining rows land on the branded coming-soon placeholder
 * until their slice ships.
 */
class GuideMenuController(
    private val actions: ChannelActions,
    private val zapAway: (ChannelEntity) -> Unit,
    private val focusedRow: () -> GuideRow?,
    private val info: () -> GuideInfoData?,
    private val callbacks: GuideCallbacks,
) {
    private val mutable = MutableStateFlow<GuideLayer>(GuideLayer.Grid)
    val layer: StateFlow<GuideLayer> = mutable.asStateFlow()

    fun show(next: GuideLayer) {
        mutable.value = next
    }

    fun reset() = show(GuideLayer.Grid)

    /** BACK pops one level: pushed screens return to the sheet, the sheet to the grid. */
    fun close() = show(backOf(mutable.value))

    /** Long-OK / MENU with a focused row opens the sheet (round3-ref 05). */
    fun openRowMenu() {
        if (focusedRow() != null) show(GuideLayer.RowMenu)
    }

    /** Every dropdown row is premium in the free reference (capture 31). */
    fun onCellAction(action: GuideCellAction) = show(GuideLayer.Paywall(action.label))

    /** All §41 pane rows are locked; only Unlock Premium is focusable. */
    fun onChannelOption(rowId: String) {
        if (rowId == RowIds.UNLOCK_PREMIUM) show(GuideLayer.Paywall("Channel options", back = mutable.value))
    }

    fun onMenuItem(item: PlayerMenuItem) {
        val row = focusedRow() ?: return
        when (PlayerMenuRouting.routeOf(item)) {
            PlayerMenuRoute.SEARCH -> {
                reset()
                callbacks.onOpenSearch()
            }
            PlayerMenuRoute.SETTINGS -> callbacks.onOpenSettings()
            PlayerMenuRoute.TOGGLE_FAVORITE -> toggleFavorite(row.channel)
            PlayerMenuRoute.HIDE_CHANNEL -> hide(row.channel)
            PlayerMenuRoute.DESCRIPTION -> show(description())
            PlayerMenuRoute.CHANNEL_OPTIONS -> show(GuideLayer.ChannelOptions(row.channel.source.name))
            PlayerMenuRoute.PAYWALL -> show(GuideLayer.Paywall(item.label, back = GuideLayer.RowMenu))
            PlayerMenuRoute.COMING_SOON -> show(GuideLayer.ComingSoon(item.label))
        }
    }

    /** A sheet channel action returns to the grid, like the panel's sheet. */
    private fun toggleFavorite(channel: ChannelEntity) {
        actions.toggleFavorite(channel)
        reset()
    }

    /** Hiding the previewed channel retunes first, like the panel's sheet. */
    private fun hide(channel: ChannelEntity) {
        zapAway(channel)
        actions.hide(channel)
        reset()
    }

    private fun description(): GuideLayer.Description {
        val data = info()
        return GuideLayer.Description(
            title = data?.title ?: GuideInfoBuilder.NO_INFORMATION,
            text = data?.description ?: GuideInfoBuilder.NO_INFORMATION,
        )
    }
}

/** One BACK level per layer: pushed screens → sheet, everything else → grid. */
private fun backOf(layer: GuideLayer): GuideLayer =
    when (layer) {
        is GuideLayer.Paywall -> layer.back
        is GuideLayer.ComingSoon, is GuideLayer.Description, is GuideLayer.ChannelOptions -> GuideLayer.RowMenu
        else -> GuideLayer.Grid
    }
