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

    /** Pushes the §41 "Channel options" pane, every row locked. */
    CHANNEL_OPTIONS,

    /** Name editor that creates an empty custom group. */
    CREATE_GROUP,

    /** Rename/Delete of the selected group (locked on playlist groups). */
    GROUP_OPTIONS,

    /** Multi-select channel list copying into a custom group. */
    COPY_CHANNELS,

    /** Per-channel EPG-id picker persisting the epgOverride column. */
    ASSIGN_EPG,

    /** Bulk blocked-flag editor, PIN-gated when parental controls are on. */
    MANAGE_BLOCKING,

    /** Bulk hidden-flag editor over every channel. */
    MANAGE_VISIBILITY,

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
    /** The six routes [com.johncorser.telly.features.groups.GroupTools] serves. */
    val GROUP_TOOL_ROUTES: Set<PlayerMenuRoute> =
        setOf(
            PlayerMenuRoute.CREATE_GROUP,
            PlayerMenuRoute.GROUP_OPTIONS,
            PlayerMenuRoute.COPY_CHANNELS,
            PlayerMenuRoute.ASSIGN_EPG,
            PlayerMenuRoute.MANAGE_BLOCKING,
            PlayerMenuRoute.MANAGE_VISIBILITY,
        )

    fun routeOf(item: PlayerMenuItem): PlayerMenuRoute =
        when (item) {
            PlayerMenuItem.SEARCH -> PlayerMenuRoute.SEARCH
            PlayerMenuItem.SETTINGS -> PlayerMenuRoute.SETTINGS
            PlayerMenuItem.ADD_TO_FAVORITES -> PlayerMenuRoute.TOGGLE_FAVORITE
            PlayerMenuItem.HIDE_CHANNEL -> PlayerMenuRoute.HIDE_CHANNEL
            PlayerMenuItem.PROGRAM_DESCRIPTION -> PlayerMenuRoute.DESCRIPTION
            PlayerMenuItem.CHANNEL_OPTIONS -> PlayerMenuRoute.CHANNEL_OPTIONS
            PlayerMenuItem.CREATE_GROUP -> PlayerMenuRoute.CREATE_GROUP
            PlayerMenuItem.GROUP_OPTIONS -> PlayerMenuRoute.GROUP_OPTIONS
            PlayerMenuItem.COPY_CHANNELS -> PlayerMenuRoute.COPY_CHANNELS
            PlayerMenuItem.ASSIGN_EPG -> PlayerMenuRoute.ASSIGN_EPG
            PlayerMenuItem.MANAGE_BLOCKING -> PlayerMenuRoute.MANAGE_BLOCKING
            PlayerMenuItem.MANAGE_VISIBILITY -> PlayerMenuRoute.MANAGE_VISIBILITY
            else -> PlayerMenuRoute.COMING_SOON
        }
}
