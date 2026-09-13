package com.johncorser.telly

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme
import com.johncorser.telly.core.design.TELLY_ACCENT
import com.johncorser.telly.core.design.TELLY_GUIDANCE_PANE
import com.johncorser.telly.core.design.TELLY_ONBOARDING_BACKGROUND
import com.johncorser.telly.core.navigation.Navigator
import com.johncorser.telly.core.navigation.Route
import com.johncorser.telly.features.onboarding.SettingsScreen
import com.johncorser.telly.features.onboarding.WelcomeScreen
import com.johncorser.telly.features.onboarding.WizardScreen
import com.johncorser.telly.features.playback.PlaybackDeps
import com.johncorser.telly.features.playback.PlaybackScreen
import com.johncorser.telly.features.playlist.PlaylistRepository

/** Renders the top of the navigator's back stack and owns global BACK. */
@Composable
fun RootScreen(
    navigator: Navigator,
    repository: PlaylistRepository,
    fetchPlaylist: suspend (String) -> String,
    playbackDeps: PlaybackDeps,
) {
    val stack by navigator.stack.collectAsState()
    val route = stack.last()
    BackHandler(enabled = stack.size > 1 && route != Route.AddPlaylistWizard) { navigator.pop() }
    MaterialTheme(
        colorScheme =
            darkColorScheme(
                primary = Color(TELLY_ACCENT),
                background = Color(TELLY_ONBOARDING_BACKGROUND),
                surface = Color(TELLY_GUIDANCE_PANE),
            ),
    ) {
        when (route) {
            Route.Welcome ->
                WelcomeScreen(
                    onAddPlaylist = { navigator.push(Route.AddPlaylistWizard) },
                    onOpenSettings = { navigator.push(Route.Settings) },
                )
            Route.Settings -> SettingsScreen()
            Route.AddPlaylistWizard ->
                WizardScreen(
                    repository = repository,
                    fetchPlaylist = fetchPlaylist,
                    onExit = { navigator.pop() },
                    onComplete = { navigator.replaceAll(Route.Playback) },
                )
            Route.Playback -> PlaybackScreen(playbackDeps)
        }
    }
}
