package com.johncorser.telly.core.navigation

/** Typed destinations for the app's hand-rolled back stack. */
sealed interface Route {
    /** First-run landing screen ("telly doesn't provide any sources..."). */
    data object Welcome : Route

    /** Placeholder settings screen; real settings arrive in a later slice. */
    data object Settings : Route

    /** The add-playlist wizard (type chooser -> URL -> processing -> name). */
    data object AddPlaylistWizard : Route

    /** Post-wizard stub main screen showing channel/group counts. TiviMate
     * continues to an EPG-source step and the TV guide here; both arrive in
     * later slices (the EPG URL is already taken from the playlist's
     * `url-tvg` hint). */
    data class ChannelsLoaded(
        val channelCount: Int,
        val groupCount: Int,
    ) : Route
}
