package com.johncorser.telly.features.guide

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED
import com.johncorser.telly.core.ui.LocalAccentColor
import com.johncorser.telly.core.ui.TellyScreenBlockedLock
import com.johncorser.telly.core.ui.TellyScreenLogoTile
import com.johncorser.telly.core.ui.TellyScreenRowLabel
import com.johncorser.telly.features.playlist.db.displayName

/**
 * The grid row's fixed channel column: number, 45×30 dp logo tile and the
 * (custom) display name, accent blue + ▶ while previewed (capture 32).
 * A name too long for the column marquees while D-pad focus sits on the row.
 */
@Composable
internal fun GuideScreenChannelColumn(
    row: GuideRow,
    playing: Boolean,
    focused: Boolean,
) {
    val accent = LocalAccentColor.current
    Row(
        Modifier
            .width(GuideGeometry.CHANNEL_COLUMN_DP.dp)
            .fillMaxHeight()
            .padding(start = 16.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Appearance -> TV guide -> Show channel numbers (on = today).
        if (LocalGuideStyle.current.showChannelNumbers) {
            TellyScreenRowLabel(
                text = row.displayNumber.toString(),
                color = if (playing) accent else Color(TELLY_TEXT_MUTED),
                fontSize = 17.sp,
                modifier = Modifier.width(28.dp),
            )
            Spacer(Modifier.width(8.dp))
        }
        TellyScreenLogoTile(
            logoUrl = row.channel.source.logoUrl,
            name = row.channel.displayName,
            size = 30.dp,
            width = 45.dp,
        )
        Spacer(Modifier.width(10.dp))
        TellyScreenRowLabel(
            text = row.channel.displayName,
            color = if (playing) accent else Color.White,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f),
            marquee = focused,
        )
        if (row.channel.flags.blocked) TellyScreenBlockedLock()
        if (playing) TellyScreenRowLabel(text = "▶", color = accent, fontSize = 10.sp)
    }
}
