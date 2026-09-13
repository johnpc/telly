package com.johncorser.telly.core.navigation

/** Typed destinations for the app's hand-rolled back stack. */
sealed interface Route {
    /** First-run landing screen ("telly doesn't provide any sources..."). */
    data object Welcome : Route

    /** Placeholder settings screen; real settings arrive in a later slice. */
    data object Settings : Route

    /** The add-playlist wizard (type chooser -> URL -> processing). */
    data object AddPlaylistWizard : Route

    /** Fullscreen playback of the last-watched channel; the app's main screen. */
    data object Playback : Route
}
