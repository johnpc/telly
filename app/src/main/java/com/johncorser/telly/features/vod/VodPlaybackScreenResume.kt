package com.johncorser.telly.features.vod

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.johncorser.telly.R
import com.johncorser.telly.features.settings.SettingsScreenGuidedStep

/**
 * Resume/Start-over GuidedStep shown when a stored position sits inside
 * the resume band (ux-spec §VOD; shaped after the captured delete-playlist
 * confirm — the reference's VOD prompt is premium/uncaptured).
 */
@Composable
internal fun VodPlaybackScreenResume(viewModel: VodPlaybackViewModel) {
    val item by viewModel.item.collectAsState()
    val stage by viewModel.stage.collectAsState()
    val position = (stage as? VodStage.ResumePrompt)?.positionMs ?: 0
    SettingsScreenGuidedStep(
        iconRes = R.drawable.ic_rail_movie,
        title = stringResource(R.string.vod_resume_title),
        bodyLines =
            listOf(
                "Continue watching \"${item?.name.orEmpty()}\" from ${VodTimes.format(position)}",
            ),
        actions =
            listOf(
                stringResource(R.string.vod_resume) to { viewModel.resumeStored() },
                stringResource(R.string.vod_start_over) to { viewModel.startOver() },
            ),
    )
}
