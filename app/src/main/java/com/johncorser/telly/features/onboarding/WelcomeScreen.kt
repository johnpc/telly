package com.johncorser.telly.features.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.johncorser.telly.R
import com.johncorser.telly.core.ui.OnboardingScreenMessage
import com.johncorser.telly.core.ui.ScreenLifecycleStartStop

/** First-run landing screen; pixel-matched to reference screen 02-welcome. */
@Composable
fun WelcomeScreen(
    onAddPlaylist: () -> Unit,
    onOpenSettings: () -> Unit,
    restore: OnboardingRestore? = null,
) {
    val addPlaylistFocus = remember { FocusRequester() }
    val restoreFocus = remember { FocusRequester() }
    var offer by remember { mutableStateOf<RestoreOffer>(RestoreOffer.None) }
    var probeTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(probeTick) { offer = restore?.probe?.invoke() ?: RestoreOffer.None }
    // The restore pill grabs focus itself when it composes (below); this
    // covers the plain welcome and the pill disappearing after a re-probe.
    LaunchedEffect(offer) { if (offer == RestoreOffer.None) addPlaylistFocus.requestFocus() }
    // Coming back from the all-files-access grant screen re-probes.
    ScreenLifecycleStartStop(onStart = { probeTick++ }, onStop = {})
    OnboardingScreenMessage(
        headline = stringResource(R.string.welcome_headline),
        subtitle = stringResource(R.string.welcome_subtitle),
    ) {
        Spacer(Modifier.height(40.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (restore != null && offer != RestoreOffer.None) {
                WelcomeScreenRestorePill(
                    restore = restore,
                    offer = offer,
                    modifier = Modifier.focusRequester(restoreFocus),
                )
                // Focused-first: runs after the pill's node is attached.
                LaunchedEffect(Unit) { restoreFocus.requestFocus() }
            }
            WelcomeScreenPill(
                text = stringResource(R.string.welcome_add_playlist),
                onClick = onAddPlaylist,
                modifier = Modifier.focusRequester(addPlaylistFocus),
            )
            WelcomeScreenPill(
                text = stringResource(R.string.welcome_settings),
                onClick = onOpenSettings,
            )
        }
    }
}
