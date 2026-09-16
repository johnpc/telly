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
import com.johncorser.telly.features.onboarding.OnboardingRestore
import com.johncorser.telly.features.onboarding.WelcomeScreen
import com.johncorser.telly.features.onboarding.WizardScreen
import com.johncorser.telly.features.playlist.PlaylistRepository

/** The pre-guide routes: boot skeleton, welcome screen, add-playlist wizard. */
@Composable
internal fun RootScreenOnboardingRoutes(
    target: Route,
    navigator: Navigator,
    repository: PlaylistRepository,
    fetchPlaylist: suspend (String) -> String,
    restore: OnboardingRestore? = null,
) {
    when (target) {
        Route.Welcome ->
            WelcomeScreen(
                onAddPlaylist = { navigator.push(Route.AddPlaylistWizard) },
                onOpenSettings = { navigator.push(Route.Settings) },
                restore = restore,
            )
        Route.AddPlaylistWizard ->
            WizardScreen(
                repository = repository,
                fetchPlaylist = fetchPlaylist,
                onExit = { navigator.pop() },
                onComplete = { navigator.replaceAll(Route.Playback) },
            )
        else -> BootScreen()
    }
}

/** TiviMate-style boot skeleton: nothing but the app background (round3 P0 3). */
@Composable
internal fun BootScreen() {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(TELLY_ONBOARDING_BACKGROUND)),
    )
}
