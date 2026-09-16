package com.johncorser.telly.features.playback

import com.johncorser.telly.features.groups.GroupToolLauncher
import com.johncorser.telly.features.panel.PanelRow
import com.johncorser.telly.features.playlist.db.ChannelEntity

/**
 * Executes the panel sheet's rows through the shared [PlayerMenuRouting]
 * table (the guide's sheet consumes the same one, so the two can never
 * drift): favorites/hide persist via [ChannelActions], Search and Settings
 * clear the overlay chrome first, Program description pushes a screen whose
 * BACK pops back to the sheet, Channel options REPLACES the sheet — its
 * BACK lands directly on the panel, never back on the sheet (ref-round6
 * §A) — and every unbuilt row keeps the coming-soon placeholder.
 */
class PlaybackMenuHandler(
    private val sheet: SheetActions,
    private val overlays: OverlayState,
    private val tuner: TuneController,
    private val openSettings: () -> Unit = {},
    private val openSearch: () -> Unit = {},
    private val rowOf: (Long) -> PanelRow? = { null },
) {
    /** Which sheet row BACK from a pushed screen re-focuses. */
    val sheetFocus = PlayerMenuFocus()

    /** Long-OK on a panel row: a fresh sheet, focus reset to the first row. */
    fun openChannelMenu(channelId: Long) {
        sheetFocus.clear()
        overlays.set(PlaybackOverlay.ChannelMenu(channelId))
    }

    /**
     * The channel a menu action applies to: the panel row's or the tuned one,
     * re-resolved from the live list so repeated actions see fresh flags.
     */
    fun menuChannel(): ChannelEntity? {
        val id =
            (overlays.value as? PlaybackOverlay.ChannelMenu)?.channelId
                ?: tuner.current.value?.id
                ?: return null
        return tuner.byId(id) ?: tuner.current.value
    }

    fun onMenuItem(item: PlayerMenuItem) {
        val channel = menuChannel() ?: return
        sheetFocus.onActivated(item)
        when (val route = PlayerMenuRouting.routeOf(item)) {
            PlayerMenuRoute.SEARCH -> openScreen(openSearch)
            PlayerMenuRoute.SETTINGS -> openScreen(openSettings)
            PlayerMenuRoute.TOGGLE_FAVORITE -> toggleFavorite(channel)
            PlayerMenuRoute.HIDE_CHANNEL -> hide(channel)
            PlayerMenuRoute.DESCRIPTION -> push { back -> descriptionOverlay(rowOf(channel.id), back) }
            PlayerMenuRoute.CHANNEL_OPTIONS ->
                overlays.set(PlaybackOverlay.ChannelOptions(channel.source.name, back = afterAction()))
            else -> openGroupTool(item, route, channel)
        }
    }

    /** A pushed group/bulk tool (or coming-soon for the unserved rows). */
    private fun openGroupTool(
        item: PlayerMenuItem,
        route: PlayerMenuRoute,
        channel: ChannelEntity,
    ) {
        val exit = afterAction()
        val session = sheet.groupTools?.session(route, channel, onDone = { overlays.set(exit) })
        if (session == null) {
            push { back -> PlaybackOverlay.ComingSoon(item.label, back) }
        } else {
            push { back -> PlaybackOverlay.GroupTool(session, back) }
        }
    }

    /** All §41 pane rows are locked; any activation lands on coming-soon. */
    fun onChannelOption(rowId: String) = push { back -> PlaybackOverlay.ComingSoon(rowId, back) }

    /** Pushed screens remember the overlay behind them; BACK pops to it. */
    private fun push(next: (back: PlaybackOverlay) -> PlaybackOverlay) = overlays.set(next(overlays.value))

    /**
     * Search and the settings sheet open over bare playback (no stale
     * panel/menu chrome behind the scrim).
     */
    private fun openScreen(open: () -> Unit) {
        overlays.set(PlaybackOverlay.None)
        open()
    }

    private fun toggleFavorite(channel: ChannelEntity) {
        val next = afterAction()
        sheet.channels.toggleFavorite(channel)
        overlays.set(next)
    }

    private fun hide(channel: ChannelEntity) {
        val next = afterAction()
        tuner.zapAwayFrom(channel)
        sheet.channels.hide(channel)
        overlays.set(next)
    }

    /**
     * Where a row that dismisses the sheet lands: the panel behind the
     * channel menu, bare playback otherwise. Channel options shares this —
     * the pane replaces the sheet, so its BACK target is the panel too.
     */
    private fun afterAction(): PlaybackOverlay =
        if (overlays.value is PlaybackOverlay.ChannelMenu) PlaybackOverlay.Panel else PlaybackOverlay.None
}

/**
 * What the panel sheet's channel rows act through: favorite/hide
 * persistence and the launcher behind the six group/bulk tool rows.
 */
class SheetActions(
    val channels: ChannelActions,
    val groupTools: GroupToolLauncher? = null,
)

/** The sheet row's airing programme: title + synopsis (dump 40). */
private fun descriptionOverlay(
    row: PanelRow?,
    back: PlaybackOverlay,
): PlaybackOverlay =
    PlaybackOverlay.Description(
        title = row?.nowTitle ?: PlayerMenu.NO_INFORMATION,
        text = row?.description ?: PlayerMenu.NO_INFORMATION,
        back = back,
    )
