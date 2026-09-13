package com.johncorser.telly.features.onboarding

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.johncorser.telly.core.design.TELLY_TEXT_PRIMARY

/** Shown while the playlist downloads and parses. Per the capture catalogue
 * (screen 11) an indeterminate circular spinner replaces the actions column;
 * no reference screenshot captured mid-processing, so the spinner is a plain
 * rotating 270-degree arc centered in the middle column. */
@Composable
fun WizardScreenProcessing(modifier: Modifier = Modifier) {
    val spin = rememberInfiniteTransition(label = "processing")
    val angle by spin.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Restart),
        label = "spin",
    )
    Row(modifier) {
        Box(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(48.dp).rotate(angle)) {
                drawArc(
                    color = Color(TELLY_TEXT_PRIMARY),
                    startAngle = 0f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
                )
            }
        }
        WizardScreenButtonsPane {}
    }
}
