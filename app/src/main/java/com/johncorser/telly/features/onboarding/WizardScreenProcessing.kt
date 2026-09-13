package com.johncorser.telly.features.onboarding

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.R
import com.johncorser.telly.core.design.TELLY_FIELD_UNDERLINE
import com.johncorser.telly.core.design.TELLY_FOCUS_FILL
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED

/** Shown while the playlist downloads and parses; no reference screenshot
 * captured this state (11 already shows the processed step), so it reuses the
 * wizard's action-pane metrics with an indeterminate sweep bar. */
@Composable
fun WizardScreenProcessing(modifier: Modifier = Modifier) {
    val sweep = rememberInfiniteTransition(label = "processing")
    val progress by sweep.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "sweep",
    )
    Row(modifier) {
        WizardScreenActionsPane(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.wizard_processing),
                color = Color(TELLY_TEXT_MUTED),
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 14.dp),
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier =
                    Modifier
                        .padding(start = 14.dp)
                        .width(262.dp)
                        .height(4.dp)
                        .background(Color(TELLY_FIELD_UNDERLINE)),
            ) {
                Box(
                    modifier =
                        Modifier
                            .offset(x = 182.dp * progress)
                            .width(80.dp)
                            .fillMaxHeight()
                            .background(Color(TELLY_FOCUS_FILL)),
                )
            }
        }
        WizardScreenButtonsPane {}
    }
}
