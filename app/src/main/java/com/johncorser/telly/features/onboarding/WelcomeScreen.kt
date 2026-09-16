package com.johncorser.telly.features.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.johncorser.telly.R
import com.johncorser.telly.core.ui.OnboardingScreenMessage
import com.johncorser.telly.core.ui.focusOnAppear

/** First-run landing screen; pixel-matched to reference screen 02-welcome. */
@Composable
fun WelcomeScreen(
    onAddPlaylist: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    OnboardingScreenMessage(
        headline = stringResource(R.string.welcome_headline),
        subtitle = stringResource(R.string.welcome_subtitle),
    ) {
        Spacer(Modifier.height(40.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            WelcomeScreenPill(
                text = stringResource(R.string.welcome_add_playlist),
                onClick = onAddPlaylist,
                modifier = Modifier.focusOnAppear(),
            )
            WelcomeScreenPill(
                text = stringResource(R.string.welcome_settings),
                onClick = onOpenSettings,
            )
        }
    }
}
