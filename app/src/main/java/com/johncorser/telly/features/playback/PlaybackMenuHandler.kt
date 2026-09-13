package com.johncorser.telly.features.playback

import com.johncorser.telly.features.playlist.db.ChannelEntity

/**
 * Executes context-menu selections. Favorites and hide are real (persisted
 * via [ChannelActions]); every other captured row routes to a branded
 * "coming soon" placeholder until its slice ships.
 */
class PlaybackMenuHandler(
    private val actions: ChannelActions,
    private val overlays: OverlayState,
    private val tuner: TuneController,
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
        when (item) {
            PlayerMenuItem.ADD_TO_FAVORITES -> toggleFavorite(channel)
            PlayerMenuItem.HIDE_CHANNEL -> hide(channel)
            else -> overlays.set(PlaybackOverlay.ComingSoon(item.label))
        }
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
