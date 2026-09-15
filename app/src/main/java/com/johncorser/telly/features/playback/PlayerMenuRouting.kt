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

    /** Opens the PIN dialog (confirm or first-time setup) then flips blocked. */
    TOGGLE_BLOCK,

    /** Shows the focused programme's title + synopsis. */
    DESCRIPTION,

    /** Pushes the §41 "Channel options" pane, every row locked. */
    CHANNEL_OPTIONS,

    /** Unbuilt row: the branded coming-soon placeholder. */
    COMING_SOON,
}

/**
 * The routing table of the shared context sheet, derived ONCE here: both
 * the guide's sheet ([com.johncorser.telly.features.guide.GuideMenuController])
 * and the playback panel's ([PlaybackMenuHandler]) consume it, so the two
 * sheets can never drift.
 */
object PlayerMenuRouting {
    fun routeOf(item: PlayerMenuItem): PlayerMenuRoute =
        when (item) {
            PlayerMenuItem.SEARCH -> PlayerMenuRoute.SEARCH
            PlayerMenuItem.SETTINGS -> PlayerMenuRoute.SETTINGS
            PlayerMenuItem.ADD_TO_FAVORITES -> PlayerMenuRoute.TOGGLE_FAVORITE
            PlayerMenuItem.HIDE_CHANNEL -> PlayerMenuRoute.HIDE_CHANNEL
            PlayerMenuItem.BLOCK_CHANNEL -> PlayerMenuRoute.TOGGLE_BLOCK
            PlayerMenuItem.PROGRAM_DESCRIPTION -> PlayerMenuRoute.DESCRIPTION
            PlayerMenuItem.CHANNEL_OPTIONS -> PlayerMenuRoute.CHANNEL_OPTIONS
            else -> PlayerMenuRoute.COMING_SOON
        }
}
