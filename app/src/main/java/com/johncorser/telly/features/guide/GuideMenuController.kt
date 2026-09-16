package com.johncorser.telly.features.guide

import com.johncorser.telly.features.groups.GroupToolLauncher
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
    private val sheet: GuideSheetChannelActions,
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

    /** Every dropdown row is an unbuilt feature: coming-soon placeholder. */
    fun onCellAction(action: GuideCellAction) = show(GuideLayer.ComingSoon(action.label))

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
            PlayerMenuRoute.DESCRIPTION -> show(descriptionLayer(info()))
            PlayerMenuRoute.CHANNEL_OPTIONS -> show(GuideLayer.ChannelOptions(row.channel.source.name))
            else -> openGroupTool(route, item, row.channel)
        }
    }

    /**
     * A group/bulk tool pushed over the sheet; finishing an action → grid.
     * Routes the tools don't serve (the still-unbuilt rows) fall through
     * to the branded coming-soon placeholder that backs to the sheet.
     */
    private fun openGroupTool(
        route: PlayerMenuRoute,
        item: PlayerMenuItem,
        channel: ChannelEntity,
    ) {
        val session = sheet.groupTools?.session(route, channel, onDone = ::reset)
        show(session?.let { GuideLayer.GroupTool(it) } ?: GuideLayer.ComingSoon(item.label, back = GuideLayer.RowMenu))
    }

    /** A sheet channel action returns to the grid, like the panel's sheet. */
    private fun toggleFavorite(channel: ChannelEntity) {
        sheet.actions.toggleFavorite(channel)
        reset()
    }

    /** Hiding the previewed channel retunes first, like the panel's sheet. */
    private fun hide(channel: ChannelEntity) {
        sheet.zapAway(channel)
        sheet.actions.hide(channel)
        reset()
    }
}

/**
 * What the guide sheet's channel rows act through: favorite/hide
 * persistence, the zap-away used before hiding the previewed channel, and
 * the launcher behind the six group/bulk tool rows.
 */
class GuideSheetChannelActions(
    val actions: ChannelActions,
    val zapAway: (ChannelEntity) -> Unit,
    val groupTools: GroupToolLauncher? = null,
)

/** The sheet's working "Program description" row over the focused cell. */
private fun descriptionLayer(data: GuideInfoData?): GuideLayer.Description =
    GuideLayer.Description(
        title = data?.title ?: GuideInfoBuilder.NO_INFORMATION,
        text = data?.description ?: GuideInfoBuilder.NO_INFORMATION,
    )

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
        is GuideLayer.GroupTool -> GuideLayer.RowMenu
        else -> GuideLayer.Grid
    }
