package com.johncorser.telly.features.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.R
import com.johncorser.telly.core.design.TELLY_ONBOARDING_BACKGROUND
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED

/** Empty placeholder; the real settings tree is a later slice. */
@Composable
fun SettingsScreen() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(TELLY_ONBOARDING_BACKGROUND)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            color = Color(TELLY_TEXT_MUTED),
            fontSize = 20.sp,
        )
    }
}
