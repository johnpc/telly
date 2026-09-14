package com.johncorser.telly.features.guide

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED
import com.johncorser.telly.core.ui.LocalAccentColor
import com.johncorser.telly.core.ui.TellyScreenLogoTile

/**
 * One 39 dp grid row (uidump 24, 78 px pitch): number, 45×30 dp logo tile
 * and name in the fixed channel column, then the cell strip clipped to the
 * time viewport. The previewed channel's number/name go accent blue with a
 * ▶ marker.
 */
@Composable
internal fun GuideScreenRow(
    row: GuideRow,
    focusedCell: GuideCell?,
    playing: Boolean,
    scrollXDp: Float,
    originMs: Long,
    nowMs: Long,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(GuideGeometry.ROW_HEIGHT_DP.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GuideScreenChannelColumn(row, playing)
        Box(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .clipToBounds(),
        ) {
            row.cells.forEach { cell ->
                GuideCellLayout.place(cell, originMs, scrollXDp, GuideGeometry.TIME_VIEWPORT_DP)?.let { placement ->
                    GuideScreenCell(
                        cell = cell,
                        placement = placement,
                        focused = cell.startMs == focusedCell?.startMs,
                        past = cell.endMs <= nowMs,
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideScreenChannelColumn(
    row: GuideRow,
    playing: Boolean,
) {
    val nameColor = if (playing) LocalAccentColor.current else Color.White
    Row(
        Modifier
            .width(GuideGeometry.CHANNEL_COLUMN_DP.dp)
            .fillMaxHeight()
            .padding(start = 16.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = row.displayNumber.toString(),
            modifier = Modifier.width(26.dp),
            color = if (playing) LocalAccentColor.current else Color(TELLY_TEXT_MUTED),
            fontSize = 14.sp,
        )
        TellyScreenLogoTile(
            logoUrl = row.channel.source.logoUrl,
            name = row.channel.source.name,
            size = 30.dp,
            width = 45.dp,
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = row.channel.source.name,
            modifier = Modifier.weight(1f),
            color = nameColor,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (playing) Text(text = "▶", color = LocalAccentColor.current, fontSize = 10.sp)
    }
}
