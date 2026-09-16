package com.johncorser.telly.core.navigation

/** Typed destinations for the app's hand-rolled back stack. */
sealed interface Route {
    /** Cold-start skeleton (#131619 frame) shown while Room resolves. */
    data object Boot : Route

    /** First-run landing screen ("telly doesn't provide any sources..."). */
    data object Welcome : Route

    /** Placeholder settings screen; real settings arrive in a later slice. */
    data object Settings : Route

    /** The add-playlist wizard (type chooser -> URL -> processing -> name). */
    data object AddPlaylistWizard : Route

    /** Fullscreen playback of the last-watched channel; the app's main screen. */
    data object Playback : Route

    /** The TV guide: preview window + info pane over the programme grid. */
    data object Guide : Route

    /** TiviMate-style search screen (catalogue §4, captures 49-51). */
    data object Search : Route

    /** Multiview grid from the quick-bar's Multiview slot (multiview-round). */
    data object Multiview : Route

    /**
     * The full-screen History list behind the info overlay's History card
     * (history-round2 §3): pushed over playback, BACK pops to the player.
     */
    data object History : Route

    /** Saved programmes behind the guide rail's bookmark icon (mylist). */
    data object MyList : Route

    /** The context sheet's "Manage Favorites" screen (mylist slice). */
    data object ManageFavorites : Route

    /** The context sheet's "Reorder channels" screen, on the sheet's group. */
    data class ReorderChannels(
        val group: String,
    ) : Route

    /** The Channel-options pane's bulk "Channel names editor" list. */
    data object ChannelNamesEditor : Route

    /** The VOD "Movies" browser behind the guide rail's film icon. */
    data object Vod : Route

    /** Fullscreen seekable playback of one VOD item, keyed for resume. */
    data class VodPlayback(
        val itemKey: String,
    ) : Route

    /**
     * The Recordings / DVR library (recording slice): reached from the
     * quick-bar's Recordings slot and the guide rail's DVR icon; BACK pops
     * back to wherever it was opened from.
     */
    data object Recordings : Route
}
