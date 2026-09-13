package com.johncorser.telly.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_ONBOARDING_BACKGROUND
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED
import com.johncorser.telly.core.design.TELLY_TEXT_PRIMARY

/**
 * Full-screen centered headline + grey subtitle on the onboarding background
 * (reference screen 02), with an optional slot for content below. The 3 dp
 * downward offset matches TiviMate's block position (punch-list items 8/9).
 */
@Composable
fun OnboardingScreenMessage(
    headline: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color(TELLY_ONBOARDING_BACKGROUND))
                .offset(y = 3.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = headline,
            color = Color(TELLY_TEXT_PRIMARY),
            fontSize = 20.sp,
            letterSpacing = 0.sp,
            // TiviMate renders the headline 3.5 dp higher relative to the
            // subtitle than Compose's default line box does (measured).
            modifier = Modifier.offset(y = (-3.5).dp),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            color = Color(TELLY_TEXT_MUTED),
            fontSize = 18.sp,
            letterSpacing = 0.sp,
        )
        content()
    }
}
