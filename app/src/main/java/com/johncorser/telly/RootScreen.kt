package com.johncorser.telly

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme
import com.johncorser.telly.core.design.TELLY_GUIDANCE_PANE
import com.johncorser.telly.core.design.TELLY_ONBOARDING_BACKGROUND
import com.johncorser.telly.core.navigation.Navigator
import com.johncorser.telly.core.navigation.Route
import com.johncorser.telly.core.ui.ProvideAccentColor
import com.johncorser.telly.core.ui.ScreenCrossfade
import com.johncorser.telly.features.guide.GuideDeps
import com.johncorser.telly.features.guide.GuideScreen
import com.johncorser.telly.features.onboarding.WelcomeScreen
import com.johncorser.telly.features.onboarding.WizardScreen
import com.johncorser.telly.features.playback.PlaybackDeps
import com.johncorser.telly.features.playback.PlaybackScreen
import com.johncorser.telly.features.playlist.PlaylistRepository
import com.johncorser.telly.features.settings.SettingsGraph
import com.johncorser.telly.features.settings.SettingsScreenHost

/** Renders the top of the navigator's back stack and owns global BACK. */
@Composable
fun RootScreen(
    navigator: Navigator,
    repository: PlaylistRepository,
    fetchPlaylist: suspend (String) -> String,
    playbackDeps: PlaybackDeps,
    guideDeps: GuideDeps,
    settingsGraph: SettingsGraph,
) {
    val stack by navigator.stack.collectAsState()
    val route = stack.last()
    BackHandler(enabled = stack.size > 1 && route != Route.AddPlaylistWizard) { navigator.pop() }
    ProvideAccentColor(settingsGraph.settings) { accent ->
        MaterialTheme(
            colorScheme =
                darkColorScheme(
                    primary = accent,
                    background = Color(TELLY_ONBOARDING_BACKGROUND),
                    surface = Color(TELLY_GUIDANCE_PANE),
                ),
        ) {
            ScreenCrossfade(route) { target ->
                when (target) {
                    Route.Welcome ->
                        WelcomeScreen(
                            onAddPlaylist = { navigator.push(Route.AddPlaylistWizard) },
                            onOpenSettings = { navigator.push(Route.Settings) },
                        )
                    Route.Settings ->
                        SettingsScreenHost(
                            graph = settingsGraph,
                            onAddPlaylist = { navigator.push(Route.AddPlaylistWizard) },
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
                    // confirmation, the device-verified free-tier BACK chain.
                    Route.Playback ->
                        PlaybackScreen(
                            deps = playbackDeps,
                            onExitToGuide = { navigator.replaceAll(Route.Guide) },
                        )
                    Route.Guide ->
                        GuideScreen(
                            deps = guideDeps,
                            onFullscreen = { navigator.push(Route.Playback) },
                        )
                }
            }
        }
    }
}
