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

    /** Fires the external-player chooser with the channel's stream URL. */
    EXTERNAL_PLAYER,

    /** Replaces the sheet with the live §41 "Channel options" pane. */
    CHANNEL_OPTIONS,

    /** Saves/removes the focused programme; the row label flips (mylist). */
    MY_LIST_TOGGLE,

    /** Opens the Manage Favorites screen (Route.ManageFavorites). */
    MANAGE_FAVORITES,

    /** Opens the reorder screen on the sheet's group (Route.ReorderChannels). */
    REORDER_CHANNELS,

    /** Instant record of the channel (or Stop confirm while it records). */
    RECORD,

    /** The custom-recording form (channel prefilled, start + duration). */
    CUSTOM_RECORDING,

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

    /** Every sheet row is live: the table is TOTAL (no coming-soon fallback). */
    private val routes: Map<PlayerMenuItem, PlayerMenuRoute> =
        mapOf(
            PlayerMenuItem.SEARCH to PlayerMenuRoute.SEARCH,
            PlayerMenuItem.SETTINGS to PlayerMenuRoute.SETTINGS,
            PlayerMenuItem.ADD_TO_FAVORITES to PlayerMenuRoute.TOGGLE_FAVORITE,
            PlayerMenuItem.HIDE_CHANNEL to PlayerMenuRoute.HIDE_CHANNEL,
            PlayerMenuItem.OPEN_IN_EXTERNAL_PLAYER to PlayerMenuRoute.EXTERNAL_PLAYER,
            PlayerMenuItem.BLOCK_CHANNEL to PlayerMenuRoute.TOGGLE_BLOCK,
            PlayerMenuItem.PROGRAM_DESCRIPTION to PlayerMenuRoute.DESCRIPTION,
            PlayerMenuItem.CHANNEL_OPTIONS to PlayerMenuRoute.CHANNEL_OPTIONS,
            PlayerMenuItem.ADD_TO_MY_LIST to PlayerMenuRoute.MY_LIST_TOGGLE,
            PlayerMenuItem.MANAGE_FAVORITES to PlayerMenuRoute.MANAGE_FAVORITES,
            PlayerMenuItem.REORDER_CHANNELS to PlayerMenuRoute.REORDER_CHANNELS,
            PlayerMenuItem.RECORD to PlayerMenuRoute.RECORD,
            PlayerMenuItem.CUSTOM_RECORDING to PlayerMenuRoute.CUSTOM_RECORDING,
            PlayerMenuItem.CREATE_GROUP to PlayerMenuRoute.CREATE_GROUP,
            PlayerMenuItem.GROUP_OPTIONS to PlayerMenuRoute.GROUP_OPTIONS,
            PlayerMenuItem.COPY_CHANNELS to PlayerMenuRoute.COPY_CHANNELS,
            PlayerMenuItem.ASSIGN_EPG to PlayerMenuRoute.ASSIGN_EPG,
            PlayerMenuItem.MANAGE_BLOCKING to PlayerMenuRoute.MANAGE_BLOCKING,
            PlayerMenuItem.MANAGE_VISIBILITY to PlayerMenuRoute.MANAGE_VISIBILITY,
        )

    fun routeOf(item: PlayerMenuItem): PlayerMenuRoute = routes.getValue(item)
}
