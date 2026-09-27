package com.johncorser.telly.features.settings

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_GUIDE_TOAST

/**
 * Trailing spinner on an in-flight "Update ..." action row: a rotating
 * open arc in the row's content color (tv-material ships no progress
 * indicator). Semantics "updating" keys the acceptance assertions.
 */
@Composable
internal fun SettingsScreenRowSpinner() {
    val angle by rememberInfiniteTransition(label = "row-spinner")
        .animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(SPIN_MS, easing = LinearEasing)),
            label = "angle",
        )
    val color = LocalContentColor.current
    Canvas(Modifier.size(18.dp).semantics { contentDescription = "updating" }) {
        drawArc(
            color = color,
            startAngle = angle,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

/**
 * The refresh completion message, bottom-center in the guide hint-toast
 * treatment (light grey card, black text); the view model clears it after
 * [REFRESH_MESSAGE_MS].
 */
@Composable
internal fun SettingsScreenRefreshToast(
    model: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val status by model.refreshState.collectAsState()
    val message = status.message ?: return
    Text(
        text = message,
        modifier =
            modifier
                .padding(bottom = 24.dp)
                .background(Color(TELLY_GUIDE_TOAST), RoundedCornerShape(4.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
        color = Color.Black,
        fontSize = 15.sp,
    )
}

private const val SPIN_MS = 900
