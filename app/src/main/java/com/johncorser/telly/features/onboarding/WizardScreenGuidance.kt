package com.johncorser.telly.features.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.johncorser.telly.R
import com.johncorser.telly.core.design.TELLY_GUIDANCE_PANE
import com.johncorser.telly.core.design.TELLY_TEXT_GUIDANCE_MUTED
import com.johncorser.telly.core.design.TELLY_TEXT_PRIMARY

/** Left guidance pane: 470 dp, big icon + 36 sp step title (screens 03/07/12). */
@Composable
fun WizardScreenGuidance(state: WizardUiState) {
    val (icon, title) =
        when (state.step) {
            WizardStep.TYPE_CHOOSER -> R.drawable.ic_wizard_playlist_add to R.string.wizard_playlist_type
            WizardStep.URL_ENTRY -> R.drawable.ic_wizard_link to R.string.wizard_m3u_playlist
            WizardStep.PROCESSING -> R.drawable.ic_wizard_download to R.string.wizard_processing
            else -> R.drawable.ic_wizard_download to R.string.wizard_processed
        }
    Row(
        modifier =
            Modifier
                .width(WizardScreenDims.guidanceWidth)
                .fillMaxHeight()
                .background(Color(TELLY_GUIDANCE_PANE))
                .padding(start = 56.dp, top = 152.dp),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = Color(TELLY_TEXT_PRIMARY),
            modifier = Modifier.size(128.dp),
        )
        Spacer(Modifier.width(24.dp))
        Column(Modifier.width(198.dp).padding(top = 28.dp)) {
            Text(
                text = stringResource(title),
                color = Color(TELLY_TEXT_PRIMARY),
                fontSize = 36.sp,
                lineHeight = 48.sp,
                letterSpacing = (-0.01).em,
            )
            WizardScreenGuidanceSummary(state)
        }
    }
}

/** The "Channels: N" / "Movies: N" line under the processed-step title. */
@Composable
private fun WizardScreenGuidanceSummary(state: WizardUiState) {
    if (state.step != WizardStep.PROCESSED && state.step != WizardStep.DONE) return
    val summary =
        if (state.liveCount == 0 && state.movieCount > 0) {
            stringResource(R.string.wizard_movies_count, state.movieCount)
        } else {
            stringResource(R.string.wizard_channels_count, state.liveCount)
        }
    Text(
        text = summary,
        color = Color(TELLY_TEXT_GUIDANCE_MUTED),
        fontSize = 14.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.sp,
        modifier = Modifier.padding(top = 2.dp),
    )
}
