package com.johncorser.telly.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.johncorser.telly.core.design.TELLY_LOGO_TILE
import com.johncorser.telly.core.design.TELLY_PROGRESS_TRACK

/**
 * Playback widgets shared by the info overlay and the channel panel (rows
 * and overlay must not duplicate each other — jscpd threshold is 0).
 */
private const val PERMILLE = 1000f

/** TiviMate progress line: grey track, accent/grey fill, optional thumb. */
@Composable
fun TellyScreenProgressBar(
    permille: Int,
    modifier: Modifier = Modifier,
    thumb: Boolean = false,
    fill: Color = LocalAccentColor.current,
    thickness: Dp = 2.dp,
) {
    val fraction = permille / PERMILLE
    Box(modifier, contentAlignment = Alignment.CenterStart) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(thickness)
                .background(Color(TELLY_PROGRESS_TRACK)),
        )
        Box(
            Modifier
                .fillMaxWidth(fraction)
                .height(thickness)
                .background(fill),
        )
        if (thumb) {
            Box(Modifier.fillMaxWidth(fraction), contentAlignment = Alignment.CenterEnd) {
                Box(
                    Modifier
                        .size(8.dp)
                        .background(fill, CircleShape),
                )
            }
        }
    }
}

/** Rounded channel-logo tile with an initials fallback under the image. */
@Composable
fun TellyScreenLogoTile(
    logoUrl: String?,
    name: String,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(TELLY_LOGO_TILE)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = initialsOf(name), color = Color.White, fontSize = (size.value / 3).sp)
        logoUrl?.let {
            AsyncImage(model = it, contentDescription = null, modifier = Modifier.fillMaxSize())
        }
    }
}

private fun initialsOf(name: String): String =
    name
        .split(' ')
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
