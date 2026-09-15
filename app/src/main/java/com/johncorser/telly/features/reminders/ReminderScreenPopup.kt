package com.johncorser.telly.features.reminders

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.johncorser.telly.R
import com.johncorser.telly.core.navigation.Navigator
import com.johncorser.telly.core.navigation.Route
import com.johncorser.telly.features.settings.SettingsScreenGuidedStep

/**
 * The root-hosted reminder popup, over guide/playback/settings alike:
 * programme title + channel + "Starts at HH:MM" in the GuidedStep style.
 * Watch tunes via the search precedent (last-channel persisted by the
 * controller, playback pushed over a guide root); Dismiss and BACK close.
 */
@Composable
fun ReminderScreenPopupHost(
    hub: RemindersHub,
    navigator: Navigator,
) {
    val data by hub.popup.popup.collectAsState()
    val popup = data ?: return
    BackHandler { hub.popup.dismiss() }
    SettingsScreenGuidedStep(
        iconRes = R.drawable.ic_reminder_bell,
        title = popup.programmeTitle,
        bodyLines = listOf(popup.channelName, popup.startsAtLabel).filter { it.isNotBlank() },
        actions =
            listOf(
                "Watch" to {
                    hub.popup.watch(popup)
                    navigator.replaceAll(Route.Guide)
                    navigator.push(Route.Playback)
                },
                "Dismiss" to { hub.popup.dismiss() },
            ),
    )
}
