package com.johncorser.telly

import androidx.compose.runtime.Composable
import com.johncorser.telly.core.navigation.Navigator
import com.johncorser.telly.core.navigation.Route
import com.johncorser.telly.core.ui.ScreenCrossfade
import com.johncorser.telly.features.guide.GuideDeps
import com.johncorser.telly.features.guide.GuideScreen
import com.johncorser.telly.features.history.HistoryScreen
import com.johncorser.telly.features.multiview.MultiviewDeps
import com.johncorser.telly.features.multiview.MultiviewScreen
import com.johncorser.telly.features.playback.PlaybackDeps
import com.johncorser.telly.features.playback.PlaybackScreen
import com.johncorser.telly.features.playlist.PlaylistRepository
import com.johncorser.telly.features.search.SearchDeps
import com.johncorser.telly.features.search.SearchScreen
import com.johncorser.telly.features.vod.VodDeps

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
    multiviewDeps: MultiviewDeps,
    vodDeps: VodDeps,
    onEnterPip: () -> Unit = {},
) {
    ScreenCrossfade(baseRoute) { target ->
        when (target) {
            Route.Boot, Route.Settings, Route.Welcome, Route.AddPlaylistWizard ->
                RootScreenOnboardingRoutes(target, navigator, repository, fetchPlaylist)
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
                    onOpenMultiview = { navigator.push(Route.Multiview) },
                    onEnterPip = onEnterPip,
                    onOpenManageFavorites = { navigator.push(Route.ManageFavorites) },
                    onOpenReorderChannels = { group -> navigator.push(Route.ReorderChannels(group)) },
                )
            // BACK at the pane grid exits to fullscreen playback of the
            // focused pane's channel (multiview-spec: exit chain).
            Route.Multiview -> MultiviewScreen(deps = multiviewDeps, onExit = { navigator.pop() })
            Route.Guide ->
                GuideScreen(
                    deps = guideDeps,
                    onFullscreen = { navigator.push(Route.Playback) },
                    onOpenSearch = { navigator.push(Route.Search) },
                    onOpenSettings = { navigator.push(Route.Settings) },
                    onOpenVod = { navigator.push(Route.Vod) },
                    settingsOpen = settingsOpen,
                    onOpenMyList = { navigator.push(Route.MyList) },
                    onOpenManageFavorites = { navigator.push(Route.ManageFavorites) },
                    onOpenReorderChannels = { group -> navigator.push(Route.ReorderChannels(group)) },
                )
            Route.Vod, is Route.VodPlayback -> RootScreenVodRoutes(target, navigator, vodDeps)
            // Tuning from search adopts the guide-root BACK chain: the
            // guide becomes the stack root with fullscreen playback above.
            Route.Search ->
                SearchScreen(
                    deps = searchDeps,
                    onTuned = {
                        navigator.replaceAll(Route.Guide)
                        navigator.push(Route.Playback)
                    },
                    onOpenSettings = { navigator.push(Route.Settings) },
                )
            // A History row tunes by persisting lastChannelId (the search
            // precedent); popping recomposes playback, which restores it.
            Route.History ->
                HistoryScreen(
                    deps = playbackDeps,
                    onTuned = { navigator.pop() },
                )
            Route.MyList, Route.ManageFavorites, is Route.ReorderChannels ->
                RootScreenMyListRoutes(target, navigator, playbackDeps)
        }
    }
}
