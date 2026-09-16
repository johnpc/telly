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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.johncorser.telly.R
import com.johncorser.telly.core.ui.OnboardingScreenMessage
import com.johncorser.telly.core.ui.ScreenLifecycleStartStop
import com.johncorser.telly.core.ui.focusOnAppear
import com.johncorser.telly.core.ui.rememberFocusScreenReclaim

/** First-run landing screen; pixel-matched to reference screen 02-welcome. */
@Composable
fun WelcomeScreen(
    onAddPlaylist: () -> Unit,
    onOpenSettings: () -> Unit,
    restore: OnboardingRestore? = null,
) {
    // Add playlist is the reclaim target: it takes the initial grab (plain
    // welcome) and re-takes focus when the restore pill disappears after a
    // re-probe — both through the placement-gated engine, never a raw
    // LaunchedEffect+requestFocus (the uncatchable bring-into-view crash).
    val addPlaylistReclaim = rememberFocusScreenReclaim()
    var offer by remember { mutableStateOf<RestoreOffer>(RestoreOffer.None) }
    var probeTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(probeTick) { offer = restore?.probe?.invoke() ?: RestoreOffer.None }
    LaunchedEffect(offer) { if (restore != null && offer == RestoreOffer.None) addPlaylistReclaim.reclaim() }
    // Coming back from the all-files-access grant screen re-probes.
    ScreenLifecycleStartStop(onStart = { probeTick++ }, onStop = {})
    OnboardingScreenMessage(
        headline = stringResource(R.string.welcome_headline),
        subtitle = stringResource(R.string.welcome_subtitle),
    ) {
        Spacer(Modifier.height(40.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (restore != null && offer != RestoreOffer.None) {
                // Focused-first: the pill's appear grab wins over the
                // reclaim's initial land once the offer resolves.
                WelcomeScreenRestorePill(
                    restore = restore,
                    offer = offer,
                    modifier = Modifier.focusOnAppear(),
                )
            }
            WelcomeScreenPill(
                text = stringResource(R.string.welcome_add_playlist),
                onClick = onAddPlaylist,
                modifier = addPlaylistReclaim.target(),
            )
            WelcomeScreenPill(
                text = stringResource(R.string.welcome_settings),
                onClick = onOpenSettings,
            )
        }
    }
}
