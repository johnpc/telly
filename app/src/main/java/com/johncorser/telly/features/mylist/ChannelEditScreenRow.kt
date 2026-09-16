package com.johncorser.telly.features.mylist

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.johncorser.telly.R
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED
import com.johncorser.telly.core.ui.LocalAccentColor
import com.johncorser.telly.core.ui.TellyScreenChannelRow
import com.johncorser.telly.core.ui.TellyScreenLogoTile
import com.johncorser.telly.core.ui.focusOnAppear
import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.playlist.db.displayName

/**
 * One editor row: logo + name, and (Manage Favorites only) the star that
 * lights up in accent while the channel is a favorite. OK toggles,
 * LEFT/RIGHT moves the focused row.
 */
@Composable
internal fun ChannelEditScreenRow(
    channel: ChannelEntity,
    showStars: Boolean,
    requestFocus: Boolean,
    onToggle: (ChannelEntity) -> Unit,
    onMove: (ChannelEntity, Int) -> Unit,
) {
    TellyScreenChannelRow(
        tag = "channel-edit-row",
        onClick = { onToggle(channel) },
        modifier =
            Modifier
                .focusOnAppear(requestFocus)
                .onPreviewKeyEvent { event -> moveKey(event)?.let { onMove(channel, it) } != null },
    ) {
        TellyScreenLogoTile(logoUrl = channel.source.logoUrl, name = channel.displayName, size = 24.dp)
        Spacer(Modifier.width(12.dp))
        Text(
            text = channel.displayName,
            modifier = Modifier.weight(1f),
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (showStars) {
            Icon(
                painter = painterResource(R.drawable.ic_star_outline),
                contentDescription = if (channel.flags.favorite) "Favorite" else null,
                modifier = Modifier.size(18.dp),
                tint = if (channel.flags.favorite) LocalAccentColor.current else Color(TELLY_TEXT_MUTED),
            )
        }
    }
}

/** LEFT moves the focused row up, RIGHT down; both consume the key. */
private fun moveKey(event: KeyEvent): Int? =
    when {
        event.type != KeyEventType.KeyDown -> null
        event.key == Key.DirectionLeft -> -1
        event.key == Key.DirectionRight -> +1
        else -> null
    }
