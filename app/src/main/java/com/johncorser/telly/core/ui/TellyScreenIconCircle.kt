package com.johncorser.telly.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface

/**
 * Round icon button with TiviMate's circular focus treatment (round3-ref
 * 07/03b): transparent at rest, a white circle with a black glyph when
 * focused. Shared by the quick-bar and the transport row (jscpd 0).
 */
@Composable
fun TellyScreenIconCircle(
    icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 22.dp,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(size),
        shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
        scale = FocusScreenDefaults.scale(),
        colors = FocusScreenDefaults.colors(restingContainer = Color.Transparent),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}
