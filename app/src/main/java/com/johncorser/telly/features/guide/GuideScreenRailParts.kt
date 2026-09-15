package com.johncorser.telly.features.guide

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_TEXT_PRIMARY
import com.johncorser.telly.core.ui.FocusScreenDefaults
import com.johncorser.telly.core.ui.LocalAccentColor

/** The accent-first-letter "tv" wordmark at the top of the rail. */
@Composable
internal fun GuideScreenRailLogo(modifier: Modifier = Modifier) {
    Text(
        text =
            buildAnnotatedString {
                withStyle(SpanStyle(color = LocalAccentColor.current)) { append("t") }
                append("v")
            },
        modifier = modifier,
        color = Color.White,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
    )
}

/** A focusable rail icon (search / live-TV / bookmark / Movies / DVR / gear), pill on focus. */
@Composable
internal fun GuideScreenRailButton(
    icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    restingTint: Color = Color(TELLY_TEXT_PRIMARY),
    contentDescription: String? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = FocusScreenDefaults.shape(),
        scale = FocusScreenDefaults.scale(),
        colors = FocusScreenDefaults.colors(restingContainer = Color.Transparent, restingContent = restingTint),
    ) {
        GuideScreenRailIcon(icon, Color.Unspecified, Modifier.padding(8.dp), contentDescription)
    }
}

@Composable
internal fun GuideScreenRailIcon(
    icon: Int,
    tint: Color,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Icon(
        painter = painterResource(icon),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = if (tint == Color.Unspecified) LocalContentColor.current else tint,
    )
}
