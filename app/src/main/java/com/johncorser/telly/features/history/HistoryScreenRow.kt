package com.johncorser.telly.features.history

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Text
import com.johncorser.telly.core.ui.TellyScreenChannelRow
import com.johncorser.telly.core.ui.TellyScreenLogoTile
import com.johncorser.telly.core.ui.focusOnAppear
import com.johncorser.telly.features.playlist.db.displayName

/**
 * One History row, following the app's 39 dp channel-row idiom (the
 * reference's populated History is not capturable — history-round2 §3):
 * logo, name, the programme airing when it was last watched, and the
 * watch time. OK tunes the channel (see HistoryViewModel.tune).
 */
@Composable
internal fun HistoryScreenRow(
    row: HistoryRow,
    requestFocus: Boolean,
    onClick: () -> Unit,
) {
    TellyScreenChannelRow(
        tag = "history-row",
        onClick = onClick,
        modifier = Modifier.focusOnAppear(requestFocus),
    ) {
        TellyScreenLogoTile(
            logoUrl = row.channel.source.logoUrl,
            name = row.channel.displayName,
            size = 24.dp,
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = row.channel.displayName,
            modifier = Modifier.width(180.dp),
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = row.programmeTitle.orEmpty(),
            modifier = Modifier.weight(1f),
            color = LocalContentColor.current.copy(alpha = 0.7f),
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = row.watchedText,
            color = LocalContentColor.current.copy(alpha = 0.7f),
            fontSize = 13.sp,
            maxLines = 1,
        )
    }
}
