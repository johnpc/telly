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

    /**
     * The full-screen History list behind the info overlay's History card
     * (history-round2 §3): pushed over playback, BACK pops to the player.
     */
    data object History : Route
}
