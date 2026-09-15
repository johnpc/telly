package com.johncorser.telly.features.guide

import com.johncorser.telly.features.mylist.MyListProgramme
import com.johncorser.telly.features.playback.ChannelActions
import com.johncorser.telly.features.playback.PlayerMenuFocus
import com.johncorser.telly.features.playback.PlayerMenuItem
import com.johncorser.telly.features.playback.PlayerMenuRoute
import com.johncorser.telly.features.playback.PlayerMenuRouting
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The guide's overlay-layer state machine plus the long-OK row context
 * sheet (catalogue §3 38-42 + round3-ref 05), routed through the shared
 * [PlayerMenuRouting] table: genuinely supported rows act (Search,
 * Settings, favorites toggle, hide, the programme description); every
 * unbuilt row lands on the branded coming-soon placeholder until its
 * slice ships.
 */
class GuideMenuController(
    private val actions: ChannelActions,
    private val zapAway: (ChannelEntity) -> Unit,
    private val focusedRow: () -> GuideRow?,
    private val info: () -> GuideInfoData?,
    private val callbacks: GuideCallbacks,
    private val focusMemory: GuideFocusMemory? = null,
) {
    private val mutable = MutableStateFlow<GuideLayer>(GuideLayer.Grid)
    val layer: StateFlow<GuideLayer> = mutable.asStateFlow()

    /** Which sheet row BACK from a pushed screen re-focuses. */
    val sheetFocus = PlayerMenuFocus()

    /** The channel the sheet scrim dims AROUND (round7 P2 row exemption). */
    val sheetChannelId: Long? get() = focusMemory?.savedChannelId

    /** Closing back to the grid re-asserts the saved grid focus first. */
    fun show(next: GuideLayer) {
        if (next == GuideLayer.Grid) focusMemory?.restore()
        mutable.value = next
    }

    fun reset() = show(GuideLayer.Grid)

    /**
     * BACK pops one level: pushed screens return to the sheet, the sheet to
     * the grid — except Channel options, which REPLACED the sheet, so its
     * BACK lands directly on the grid (ref-round6 §A).
     */
    fun close() = show(backOf(mutable.value))

    /** Long-OK / MENU with a focused row opens the sheet (round3-ref 05). */
    fun openRowMenu() {
        if (focusedRow() == null) return
        focusMemory?.save()
        sheetFocus.clear()
        show(GuideLayer.RowMenu)
    }

    /**
     * The dropdown's "Add to My list" toggles the cell's programme and
     * returns to the grid; every other row is an unbuilt feature and lands
     * on the coming-soon placeholder.
     */
    fun onCellAction(action: GuideCellAction) {
        val programme = (mutable.value as? GuideLayer.CellMenu)?.cell?.program?.let(MyListProgramme::of)
        val row = focusedRow()
        if (action == GuideCellAction.ADD_TO_MY_LIST && programme != null && row != null) {
            actions.myList?.menu?.toggle(row.channel, programme)
            reset()
        } else {
            show(GuideLayer.ComingSoon(action.label))
        }
    }

    /** All §41 pane rows are locked; any activation lands on coming-soon. */
    fun onChannelOption(rowId: String) = show(GuideLayer.ComingSoon(rowId, back = mutable.value))

    fun onMenuItem(item: PlayerMenuItem) {
        val row = focusedRow() ?: return
        sheetFocus.onActivated(item)
        when (val route = PlayerMenuRouting.routeOf(item)) {
            PlayerMenuRoute.SEARCH -> {
                reset()
                callbacks.onOpenSearch()
            }
            PlayerMenuRoute.SETTINGS -> callbacks.onOpenSettings()
            PlayerMenuRoute.TOGGLE_FAVORITE -> toggleFavorite(row.channel)
            PlayerMenuRoute.HIDE_CHANNEL -> hide(row.channel)
            PlayerMenuRoute.DESCRIPTION -> show(description())
            PlayerMenuRoute.CHANNEL_OPTIONS -> show(GuideLayer.ChannelOptions(row.channel.source.name))
            // My-list rows act (or push their route) and land on the grid,
            // the management screens closing the sheet first like Search.
            PlayerMenuRoute.MY_LIST_TOGGLE,
            PlayerMenuRoute.MANAGE_FAVORITES,
            PlayerMenuRoute.REORDER_CHANNELS,
            -> {
                reset()
                actions.myList?.run(route, row.channel)
            }
            PlayerMenuRoute.COMING_SOON -> show(GuideLayer.ComingSoon(item.label, back = GuideLayer.RowMenu))
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

/**
 * One BACK level per layer: pushed screens → sheet, everything else → grid.
 * Channel options is NOT a pushed screen: in the reference it replaces the
 * sheet (in-place cross-fade), so one BACK from it lands on the grid with
 * the originating row's focus restored by [GuideFocusMemory] (ref-round6
 * §A — the reference never returns to the sheet).
 */
private fun backOf(layer: GuideLayer): GuideLayer =
    when (layer) {
        is GuideLayer.ComingSoon -> layer.back
        is GuideLayer.Description -> GuideLayer.RowMenu
        else -> GuideLayer.Grid
    }
