package com.johncorser.telly.features.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.johncorser.telly.R
import com.johncorser.telly.core.ui.OnboardingScreenMessage

/** Stub main screen shown once the wizard finishes; TV guide is a later slice. */
@Composable
fun ChannelsLoadedScreen(
    channelCount: Int,
    groupCount: Int,
) {
    OnboardingScreenMessage(
        headline = stringResource(R.string.channels_loaded, channelCount),
        subtitle = stringResource(R.string.channels_loaded_hint, groupCount),
    )
}
