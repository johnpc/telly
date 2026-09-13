package com.johncorser.telly.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.tv.material3.darkColorScheme
import com.johncorser.telly.core.design.TELLY_ACCENT
import com.johncorser.telly.core.design.TELLY_BACKGROUND
import com.johncorser.telly.core.design.TELLY_SURFACE
import com.johncorser.telly.core.design.TELLY_TEXT_SECONDARY

/** Walking-skeleton placeholder: the branded home screen shown at launch. */
@Composable
fun HomeScreen() {
    MaterialTheme(
        colorScheme =
            darkColorScheme(
                primary = Color(TELLY_ACCENT),
                background = Color(TELLY_BACKGROUND),
                surface = Color(TELLY_SURFACE),
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "telly",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Live TV, beautifully.",
                color = Color(TELLY_TEXT_SECONDARY),
                fontSize = 20.sp,
            )
        }
    }
}
