package com.johncorser.telly.features.panel

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.core.ui.TellyScreenLogoTile
import com.johncorser.telly.core.ui.TellyScreenMutedText
import com.johncorser.telly.core.ui.TellyScreenProgramTitle
import com.johncorser.telly.core.ui.TellyScreenTimesLine

/**
 * Detail card for the focused row, mirroring the expanded row of the guide
 * overlay (capture 47): big logo, programme title, times + progress +
 * remaining, group name and description.
 */
@Composable
internal fun ChannelPanelScreenDetail(row: PanelRow?) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        if (row == null) return@Row
        TellyScreenLogoTile(
            logoUrl = row.channel.source.logoUrl,
            name = row.channel.source.name,
            size = 70.dp,
        )
        Spacer(Modifier.width(16.dp))
        Column {
            TellyScreenProgramTitle(row.nowTitle)
            Spacer(Modifier.height(4.dp))
            TellyScreenTimesLine(
                range = row.nowRange,
                permille = row.progressPermille,
                remaining = row.remaining,
            ) {
                row.channel.source.groupTitle?.let { Text(text = it, color = Color.White, fontSize = 14.sp) }
            }
            Spacer(Modifier.height(4.dp))
            row.description?.let { TellyScreenMutedText(it, fontSize = 13.sp, maxLines = 2) }
        }
    }
}
