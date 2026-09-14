package com.johncorser.telly.features.playback

import com.johncorser.telly.features.panel.PanelRow
import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.settings.RowIds

/**
 * Executes the panel sheet's rows through the shared [PlayerMenuRouting]
 * table (the guide's sheet consumes the same one, so the two can never
 * drift): favorites/hide persist via [ChannelActions], Search and Settings
 * clear the overlay chrome first, premium-locked reference rows open the
 * shared Unlock Premium screen, Program description and Channel options
 * push screens whose BACK pops back to the sheet, and genuinely uncaptured
 * rows keep the branded coming-soon placeholder.
 */
class PlaybackMenuHandler(
    private val actions: ChannelActions,
    private val overlays: OverlayState,
    private val tuner: TuneController,
    private val openSettings: () -> Unit = {},
    private val openSearch: () -> Unit = {},
    private val rowOf: (Long) -> PanelRow? = { null },
) {
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
        when (PlayerMenuRouting.routeOf(item)) {
            PlayerMenuRoute.SEARCH -> openScreen(openSearch)
            PlayerMenuRoute.SETTINGS -> openScreen(openSettings)
            PlayerMenuRoute.TOGGLE_FAVORITE -> toggleFavorite(channel)
            PlayerMenuRoute.HIDE_CHANNEL -> hide(channel)
            PlayerMenuRoute.DESCRIPTION -> push { back -> description(channel, back) }
            PlayerMenuRoute.CHANNEL_OPTIONS -> push { back -> channelOptions(channel, back) }
            PlayerMenuRoute.PAYWALL -> push { back -> PlaybackOverlay.Paywall(item.label, back) }
            PlayerMenuRoute.COMING_SOON -> push { back -> PlaybackOverlay.ComingSoon(item.label, back) }
        }
    }

    /** All §41 pane rows are locked; only Unlock Premium is focusable. */
    fun onChannelOption(rowId: String) {
        if (rowId == RowIds.UNLOCK_PREMIUM) {
            push { back -> PlaybackOverlay.Paywall("Channel options", back) }
        }
    }

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

    private fun channelOptions(
        channel: ChannelEntity,
        back: PlaybackOverlay,
    ): PlaybackOverlay = PlaybackOverlay.ChannelOptions(channel.source.name, back)

    /** The sheet row's airing programme: title + synopsis (dump 40). */
    private fun description(
        channel: ChannelEntity,
        back: PlaybackOverlay,
    ): PlaybackOverlay {
        val row = rowOf(channel.id)
        return PlaybackOverlay.Description(
            title = row?.nowTitle ?: PlayerMenu.NO_INFORMATION,
            text = row?.description ?: PlayerMenu.NO_INFORMATION,
            back = back,
        )
    }

    private fun toggleFavorite(channel: ChannelEntity) {
        val next = afterAction()
        actions.toggleFavorite(channel)
        overlays.set(next)
    }

    private fun hide(channel: ChannelEntity) {
        val next = afterAction()
        tuner.zapAwayFrom(channel)
        actions.hide(channel)
        overlays.set(next)
    }

    /** A channel-menu action returns to the panel; the player menu closes. */
    private fun afterAction(): PlaybackOverlay =
        if (overlays.value is PlaybackOverlay.ChannelMenu) PlaybackOverlay.Panel else PlaybackOverlay.None
}
