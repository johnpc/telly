package com.johncorser.telly.features.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_BUTTON_RESTING
import com.johncorser.telly.core.ui.FocusScreenDefaults

/** Welcome-screen pill button: 36 dp tall, grey at rest, white when focused. */
@Composable
fun WelcomeScreenPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = FocusScreenDefaults.shape(),
        scale = FocusScreenDefaults.scale(),
        colors = FocusScreenDefaults.colors(restingContainer = Color(TELLY_BUTTON_RESTING)),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = text, fontSize = 16.sp)
        }
    }
}
