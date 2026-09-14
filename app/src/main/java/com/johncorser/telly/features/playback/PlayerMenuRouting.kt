package com.johncorser.telly.features.playback

/** Where a long-OK context-sheet row leads (catalogue §3 38-42). */
enum class PlayerMenuRoute {
    /** Closes the sheet and opens the search screen (Route.Search). */
    SEARCH,

    /** Opens the settings right sheet. */
    SETTINGS,

    /** Persists the favorite flag and returns to the sheet's host layer. */
    TOGGLE_FAVORITE,

    /** Zaps away from the watched channel first, hides, then returns. */
    HIDE_CHANNEL,

    /** Shows the focused programme's title + synopsis. */
    DESCRIPTION,

    /** Pushes the §41 "Channel options" pane, every row premium-locked. */
    CHANNEL_OPTIONS,

    /** Premium-locked reference row: the shared Unlock Premium screen. */
    PAYWALL,

    /** Genuinely uncaptured row: the branded coming-soon placeholder. */
    COMING_SOON,
}

/**
 * The routing table of the shared context sheet, derived ONCE here: both
 * the guide's sheet ([com.johncorser.telly.features.guide.GuideMenuController])
 * and the playback panel's ([PlaybackMenuHandler]) consume it, so the two
 * sheets can never drift.
 */
object PlayerMenuRouting {
    /**
     * Sheet rows the free reference locks behind Premium: the guide
     * dropdown's Record / Custom recording / Add to My list open the
     * paywall (capture 31), external playback and channel blocking are
     * locked in the Channel options pane (capture 41), and the paywall
     * body itself sells "Favorites management" and "Manual channels
     * sorting" (capture 28).
     */
    private val premium =
        setOf(
            PlayerMenuItem.OPEN_IN_EXTERNAL_PLAYER,
            PlayerMenuItem.RECORD,
            PlayerMenuItem.CUSTOM_RECORDING,
            PlayerMenuItem.ADD_TO_MY_LIST,
            PlayerMenuItem.BLOCK_CHANNEL,
            PlayerMenuItem.MANAGE_FAVORITES,
            PlayerMenuItem.REORDER_CHANNELS,
        )

    fun routeOf(item: PlayerMenuItem): PlayerMenuRoute =
        when (item) {
            PlayerMenuItem.SEARCH -> PlayerMenuRoute.SEARCH
            PlayerMenuItem.SETTINGS -> PlayerMenuRoute.SETTINGS
            PlayerMenuItem.ADD_TO_FAVORITES -> PlayerMenuRoute.TOGGLE_FAVORITE
            PlayerMenuItem.HIDE_CHANNEL -> PlayerMenuRoute.HIDE_CHANNEL
            PlayerMenuItem.PROGRAM_DESCRIPTION -> PlayerMenuRoute.DESCRIPTION
            PlayerMenuItem.CHANNEL_OPTIONS -> PlayerMenuRoute.CHANNEL_OPTIONS
            in premium -> PlayerMenuRoute.PAYWALL
            else -> PlayerMenuRoute.COMING_SOON
        }
}
