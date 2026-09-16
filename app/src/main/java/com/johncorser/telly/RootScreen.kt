package com.johncorser.telly

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme
import com.johncorser.telly.core.design.TELLY_GUIDANCE_PANE
import com.johncorser.telly.core.design.TELLY_ONBOARDING_BACKGROUND
import com.johncorser.telly.core.navigation.Navigator
import com.johncorser.telly.core.navigation.Route
import com.johncorser.telly.core.ui.ProvideAccentColor
import com.johncorser.telly.core.ui.ProvideResizeMode
import com.johncorser.telly.features.guide.GuideDeps
import com.johncorser.telly.features.multiview.MultiviewDeps
import com.johncorser.telly.features.onboarding.OnboardingRestore
import com.johncorser.telly.features.playback.PlaybackDeps
import com.johncorser.telly.features.playlist.PlaylistRepository
import com.johncorser.telly.features.recording.RecordingDeps
import com.johncorser.telly.features.reminders.ReminderScreenPopupHost
import com.johncorser.telly.features.reminders.RemindersHub
import com.johncorser.telly.features.search.SearchDeps
import com.johncorser.telly.features.settings.SettingsGraph
import com.johncorser.telly.features.settings.SettingsScreenHost
import com.johncorser.telly.features.vod.VodDeps

/** Renders the top of the navigator's back stack and owns global BACK. */
@Composable
fun RootScreen(
    navigator: Navigator,
    repository: PlaylistRepository,
    fetchPlaylist: suspend (String) -> String,
    playbackDeps: PlaybackDeps,
    guideDeps: GuideDeps,
    settingsGraph: SettingsGraph,
    searchDeps: SearchDeps,
    multiviewDeps: MultiviewDeps,
    vodDeps: VodDeps,
    onEnterPip: () -> Unit = {},
    reminders: RemindersHub? = null,
    recordingDeps: RecordingDeps? = null,
    onboardingRestore: OnboardingRestore? = null,
) {
    val stack by navigator.stack.collectAsState()
    val route = stack.last()
    // Settings is a right sheet OVER the previous screen (device-verified):
    // the base route keeps rendering (and playing) beneath the dim scrim.
    val settingsOpen = route == Route.Settings
    val baseRoute = stack.lastOrNull { it != Route.Settings } ?: route
    BackHandler(enabled = stack.size > 1 && route != Route.AddPlaylistWizard) { navigator.pop() }
    ProvideAccentColor(settingsGraph.settings) { accent ->
        ProvideResizeMode(settingsGraph.settings) {
            MaterialTheme(
                colorScheme =
                    darkColorScheme(
                        primary = accent,
                        background = Color(TELLY_ONBOARDING_BACKGROUND),
                        surface = Color(TELLY_GUIDANCE_PANE),
                    ),
            ) {
                ProvideAppearanceSettings(settingsGraph.settings) {
                    Box(Modifier.fillMaxSize()) {
                        // A reminder "Watch" bumps the epoch so a same-route tune
                        // still recreates the playback screen (cold-start path).
                        val reminderEpoch = reminders?.popup?.tuneEpoch?.collectAsState()?.value ?: 0
                        key(reminderEpoch) {
                            RootScreenRoutes(
                                baseRoute,
                                settingsOpen,
                                navigator,
                                repository,
                                fetchPlaylist,
                                playbackDeps,
                                guideDeps,
                                searchDeps,
                                multiviewDeps,
                                vodDeps,
                                onEnterPip,
                                recordingDeps,
                                onboardingRestore,
                            )
                        }
                        if (settingsOpen) {
                            SettingsScreenHost(
                                graph = settingsGraph,
                                onAddPlaylist = { navigator.push(Route.AddPlaylistWizard) },
                                onClose = { navigator.pop() },
                            )
                        }
                        if (reminders != null) ReminderScreenPopupHost(reminders, navigator)
                    }
                }
            }
        }
    }
}
