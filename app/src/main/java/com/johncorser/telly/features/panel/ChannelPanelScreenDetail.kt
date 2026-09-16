package com.johncorser.telly.features.panel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.johncorser.telly.R
import com.johncorser.telly.core.ui.TellyScreenLogoTile
import com.johncorser.telly.core.ui.TellyScreenMutedText
import com.johncorser.telly.core.ui.TellyScreenProgramTitle
import com.johncorser.telly.core.ui.TellyScreenTimesLine
import com.johncorser.telly.features.playlist.db.displayName

/**
 * The focused row's inline expansion (round3-ref 04, capture 47): 70 dp
 * logo, programme title with the favorite star at the right edge, times +
 * dash + remaining with the group name right-aligned, then the description.
 */
@Composable
internal fun ChannelPanelScreenDetail(row: PanelRow) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        TellyScreenLogoTile(
            logoUrl = row.channel.source.logoUrl,
            name = row.channel.displayName,
            size = 70.dp,
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TellyScreenProgramTitle(row.nowTitle)
                    TellyScreenTimesLine(
                        range = row.nowRange,
                        permille = row.progressPermille,
                        remaining = row.remaining,
                    ) {
                        Spacer(Modifier.weight(1f))
                        row.channel.source.groupTitle?.let {
                            Text(text = it, color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
                Icon(
                    painter = painterResource(R.drawable.ic_star_outline),
                    contentDescription = null,
                    modifier = Modifier.padding(start = 12.dp).size(20.dp),
                    tint = Color.White,
                )
            }
            row.description?.let { TellyScreenMutedText(it, fontSize = 13.sp, maxLines = 2) }
        }
    }
}
