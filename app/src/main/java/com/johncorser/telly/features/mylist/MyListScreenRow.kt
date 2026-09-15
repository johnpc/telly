package com.johncorser.telly.features.mylist

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.core.ui.LocalAccentColor
import com.johncorser.telly.core.ui.TellyScreenChannelRow
import com.johncorser.telly.core.ui.TellyScreenLogoTile
import com.johncorser.telly.core.ui.TellyScreenMutedText

/**
 * One saved programme in the app's 39 dp channel-row idiom: channel logo +
 * name, the programme title (accent-tinted while airing, the search-screen
 * precedent) and its air-time stamp. OK acts per airing state, long-OK is
 * the remove affordance.
 */
@Composable
internal fun MyListScreenRow(
    row: MyListRow,
    modifier: Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    TellyScreenChannelRow(
        tag = "mylist-row",
        onClick = onClick,
        modifier = modifier,
        onLongClick = onLongClick,
    ) {
        TellyScreenLogoTile(logoUrl = row.channel.source.logoUrl, name = row.channel.source.name, size = 24.dp)
        Spacer(Modifier.width(12.dp))
        Text(
            text = row.channel.source.name,
            modifier = Modifier.width(160.dp),
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = row.entry.title,
            modifier = Modifier.weight(1f).padding(start = 12.dp),
            color = if (row.airing) LocalAccentColor.current else Color.Unspecified,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        TellyScreenMutedText(text = row.airTimeText, fontSize = 13.sp)
    }
}
