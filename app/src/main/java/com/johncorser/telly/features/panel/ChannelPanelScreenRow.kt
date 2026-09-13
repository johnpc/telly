package com.johncorser.telly.features.panel

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_PLAYBACK_ACCENT
import com.johncorser.telly.core.ui.FocusScreenDefaults
import com.johncorser.telly.core.ui.TellyScreenLogoTile
import com.johncorser.telly.core.ui.TellyScreenProgressBar

/**
 * One 39 dp channel row (78 px pitch in the 1080p captures): number, logo,
 * name, airing programme and its progress. The playing channel renders its
 * number/name in accent blue with a play marker (captures 24/47).
 */
@Composable
internal fun ChannelPanelScreenRow(
    row: PanelRow,
    playing: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    Surface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier =
            modifier
                .fillMaxWidth()
                .height(39.dp),
        shape = FocusScreenDefaults.shape(),
        scale = FocusScreenDefaults.scale(),
        colors = FocusScreenDefaults.colors(restingContainer = Color.Transparent),
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = row.displayNumber.toString(),
                modifier = Modifier.width(28.dp),
                color = rowColor(playing),
                fontSize = 15.sp,
            )
            TellyScreenLogoTile(
                logoUrl = row.channel.source.logoUrl,
                name = row.channel.source.name,
                size = 24.dp,
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = row.channel.source.name,
                modifier = Modifier.width(130.dp),
                color = rowColor(playing),
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (playing) {
                Text(text = "▶", color = Color(TELLY_PLAYBACK_ACCENT), fontSize = 11.sp)
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = row.nowTitle.orEmpty(),
                modifier = Modifier.weight(1f),
                color = LocalContentColor.current.copy(alpha = 0.7f),
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            TellyScreenProgressBar(permille = row.progressPermille, modifier = Modifier.width(40.dp))
        }
    }
}

@Composable
private fun rowColor(playing: Boolean): Color = if (playing) Color(TELLY_PLAYBACK_ACCENT) else LocalContentColor.current
