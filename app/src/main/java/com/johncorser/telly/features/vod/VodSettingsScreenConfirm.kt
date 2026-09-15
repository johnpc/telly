package com.johncorser.telly.features.vod

import androidx.compose.runtime.Composable
import com.johncorser.telly.R
import com.johncorser.telly.features.settings.SettingsScreenGuidedStep
import com.johncorser.telly.features.settings.SettingsViewModel

/**
 * "Clear playback positions?" confirm: uncapturable (the reference locks
 * VOD behind premium), shaped after the captured delete-playlist GuidedStep.
 */
@Composable
internal fun VodSettingsScreenConfirm(model: SettingsViewModel) {
    SettingsScreenGuidedStep(
        iconRes = R.drawable.ic_settings_warning,
        title = "Clear playback positions?",
        bodyLines = listOf("Resume points for all VOD items will be removed"),
        actions =
            listOf(
                "Clear" to { model.confirmClearVodPositions() },
                "Cancel" to { model.dismissOverlay() },
            ),
    )
}
