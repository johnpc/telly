package com.johncorser.telly.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_GUIDANCE_PANE
import com.johncorser.telly.core.design.TELLY_TEXT_GUIDANCE_MUTED
import com.johncorser.telly.core.design.TELLY_TEXT_PRIMARY

/** The left guidance pane of a settings GuidedStep (icon, title, body). */
@Composable
internal fun SettingsScreenGuidedPane(
    iconRes: Int,
    title: String,
    bodyLines: List<String>,
) {
    Row(
        Modifier
            .width(470.dp)
            .fillMaxHeight()
            .background(Color(TELLY_GUIDANCE_PANE))
            .padding(start = 56.dp, top = 152.dp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = Color(TELLY_TEXT_PRIMARY),
            modifier = Modifier.requiredSize(128.dp),
        )
        Spacer(Modifier.width(24.dp))
        Column(
            Modifier
                .width(198.dp)
                .padding(top = 28.dp),
        ) {
            Text(
                text = title,
                color = Color(TELLY_TEXT_PRIMARY),
                fontSize = 36.sp,
                lineHeight = 48.sp,
                letterSpacing = (-0.01).em,
            )
            Spacer(Modifier.height(6.dp))
            bodyLines.forEach { line ->
                Text(
                    text = line,
                    color = Color(TELLY_TEXT_GUIDANCE_MUTED),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontFamily = SettingsScreenDims.fontFamily,
                )
            }
        }
    }
}
