package com.johncorser.telly

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.johncorser.telly.core.design.TELLY_ONBOARDING_BACKGROUND
import com.johncorser.telly.core.navigation.Navigator
import com.johncorser.telly.core.navigation.Route
import com.johncorser.telly.core.ui.ScreenCrossfade
import com.johncorser.telly.features.guide.GuideDeps
import com.johncorser.telly.features.guide.GuideScreen
import com.johncorser.telly.features.history.HistoryScreen
import com.johncorser.telly.features.onboarding.WelcomeScreen
import com.johncorser.telly.features.onboarding.WizardScreen
import com.johncorser.telly.features.playback.PlaybackDeps
import com.johncorser.telly.features.playback.PlaybackScreen
import com.johncorser.telly.features.playlist.PlaylistRepository
import com.johncorser.telly.features.search.SearchDeps
import com.johncorser.telly.features.search.SearchScreen

/** The crossfading base-route host under RootScreen's settings sheet. */
@Composable
internal fun RootScreenRoutes(
    baseRoute: Route,
    settingsOpen: Boolean,
    navigator: Navigator,
    repository: PlaylistRepository,
    fetchPlaylist: suspend (String) -> String,
    playbackDeps: PlaybackDeps,
    guideDeps: GuideDeps,
    searchDeps: SearchDeps,
) {
    ScreenCrossfade(baseRoute) { target ->
        when (target) {
            Route.Boot, Route.Settings -> BootScreen()
            Route.Welcome ->
                WelcomeScreen(
                    onAddPlaylist = { navigator.push(Route.AddPlaylistWizard) },
                    onOpenSettings = { navigator.push(Route.Settings) },
                )
            Route.AddPlaylistWizard ->
                WizardScreen(
                    repository = repository,
                    fetchPlaylist = fetchPlaylist,
                    onExit = { navigator.pop() },
                    onComplete = { navigator.replaceAll(Route.Playback) },
                )
            // BACK/TV-guide card leave playback for the guide as its
            // new root: BACK at guide root then exits the app with no
            // confirmation, the device-verified free-tier BACK chain. The
            // History card pushes the History screen so BACK pops straight
            // back to the fullscreen player (history-round2 §3).
            Route.Playback ->
                PlaybackScreen(
                    deps = playbackDeps,
                    onExitToGuide = { navigator.replaceAll(Route.Guide) },
                    onOpenHistory = { navigator.push(Route.History) },
                    onOpenSearch = { navigator.push(Route.Search) },
                    onOpenSettings = { navigator.push(Route.Settings) },
                )
            Route.Guide ->
                GuideScreen(
                    deps = guideDeps,
                    onFullscreen = { navigator.push(Route.Playback) },
                    onOpenSearch = { navigator.push(Route.Search) },
                    onOpenSettings = { navigator.push(Route.Settings) },
                    settingsOpen = settingsOpen,
                )
            // Tuning from search adopts the guide-root BACK chain: the
            // guide becomes the stack root with fullscreen playback above.
            Route.Search ->
                SearchScreen(
                    deps = searchDeps,
                    onTuned = {
                        navigator.replaceAll(Route.Guide)
                        navigator.push(Route.Playback)
                    },
                )
            // A History row tunes by persisting lastChannelId (the search
            // precedent); popping recomposes playback, which restores it.
            Route.History ->
                HistoryScreen(
                    deps = playbackDeps,
                    onTuned = { navigator.pop() },
                )
        }
    }
}

/** TiviMate-style boot skeleton: nothing but the app background (round3 P0 3). */
@Composable
private fun BootScreen() {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(TELLY_ONBOARDING_BACKGROUND)),
    )
}
