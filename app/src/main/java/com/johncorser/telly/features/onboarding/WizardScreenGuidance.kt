package com.johncorser.telly.features.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.johncorser.telly.R
import com.johncorser.telly.core.design.TELLY_GUIDANCE_PANE
import com.johncorser.telly.core.design.TELLY_TEXT_PRIMARY

/** Left guidance pane: 438 dp, big icon + 36 sp step title (screens 03/07). */
@Composable
fun WizardScreenGuidance(step: WizardStep) {
    val onTypeChooser = step == WizardStep.TYPE_CHOOSER
    val icon = if (onTypeChooser) R.drawable.ic_wizard_playlist_add else R.drawable.ic_wizard_link
    val title = if (onTypeChooser) R.string.wizard_playlist_type else R.string.wizard_m3u_playlist
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
        Text(
            text = stringResource(title),
            color = Color(TELLY_TEXT_PRIMARY),
            fontSize = 36.sp,
            modifier = Modifier.padding(top = 22.dp),
        )
    }
}
