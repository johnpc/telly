package com.johncorser.telly.features.playback

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_LOGO_FOCUS_BORDER
import com.johncorser.telly.core.ui.TellyScreenLogoTile

// Pieces shared by the full info overlay, the compact zap overlay and the
// quick-bar (jscpd 0): scaffold, top scrim and the logo + programme-lines block.

/** Top scrim + bottom-scrim column every playback overlay variant sits in. */
@Composable
internal fun PlaybackScreenOverlayScaffold(
    group: String?,
    clockText: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    // Appearance -> Player -> Panels transparency scales today's sampled alphas (0% = exactly today).
    val bottomAlpha = LocalPanelStyle.current.scale(0.8f)
    Box(Modifier.fillMaxSize()) {
        PlaybackScreenTopScrim(group, clockText, Modifier.align(Alignment.TopCenter))
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = bottomAlpha)))),
            content = content,
        )
    }
}

/** Top scrim: group left, date + clock right, fading out ~90 dp down (item 12). */
@Composable
internal fun PlaybackScreenTopScrim(
    group: String?,
    clockText: String,
    modifier: Modifier = Modifier,
) {
    val style = LocalPanelStyle.current
    Box(
        modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(
                Brush.verticalGradient(listOf(Color.Black.copy(alpha = style.scale(0.65f)), Color.Transparent)),
            ),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Text(text = group.orEmpty(), color = Color.White, fontSize = 16.sp)
            Spacer(Modifier.weight(1f))
            // Appearance -> Player -> Show clock (on = today).
            if (style.showClock) Text(text = clockText, color = Color.White, fontSize = 16.sp)
        }
    }
}

/**
 * Logo tile centered in its 124 dp slot (tile at x 125-283 px in the ref)
 * next to the programme lines starting at x=360 px (item 11).
 */
@Composable
internal fun PlaybackScreenInfoBlock(
    data: PlaybackInfoData,
    showBadges: Boolean,
    showDescription: Boolean,
) {
    Row(
        Modifier.padding(horizontal = 40.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(124.dp), contentAlignment = Alignment.Center) {
            TellyScreenLogoTile(
                logoUrl = data.logoUrl,
                name = data.name,
                size = 79.dp,
                modifier = Modifier.border(2.dp, Color(TELLY_LOGO_FOCUS_BORDER), RoundedCornerShape(4.dp)),
            )
        }
        Spacer(Modifier.width(16.dp))
        PlaybackScreenInfoLines(data, showBadges = showBadges, showDescription = showDescription)
    }
}
