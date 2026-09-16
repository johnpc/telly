package com.johncorser.telly.features.playback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.johncorser.telly.R
import com.johncorser.telly.core.ui.LocalAccentColor
import com.johncorser.telly.core.ui.TellyScreenLogoTile
import com.johncorser.telly.features.playlist.db.displayName

/**
 * One recent-channel card (history-round2 §1, uidump 02 at 2 px = 1 dp):
 * channel logo 100×49 over the channel's CURRENT programme title in accent
 * blue — the programme, never the channel name/number. Focus reports up so
 * the overlay can show the card's air-time + title bottom line.
 */
@Composable
internal fun PlaybackScreenRecentCard(
    card: RecentCard,
    onClick: () -> Unit,
    onFocus: (RecentCard, Boolean) -> Unit,
) {
    PlaybackScreenCardSurface(
        onClick = onClick,
        modifier =
            Modifier
                .testTag("recent-card")
                .onFocusChanged { state -> onFocus(card, state.isFocused) },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            TellyScreenLogoTile(
                logoUrl = card.channel.source.logoUrl,
                name = card.channel.displayName,
                size = 49.dp,
                width = 100.dp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = card.nowTitle.orEmpty(),
                color = LocalAccentColor.current,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** The focused card's bottom line: `air window + programme title` (uidump 05). */
internal fun recentCardLine(card: RecentCard?): String? {
    val title = card?.nowTitle ?: return null
    return card.nowRange?.let { "$it   $title" } ?: title
}

/**
 * Under the cards row: a focused recent card swaps the centered chevron
 * (round3-ref 02 + item 20) for its full-width air-time + title line
 * (history-round2 uidump 05: x = 40 dp, just past the row's bottom edge).
 */
@Composable
internal fun BoxScope.PlaybackScreenCardsFooter(line: String?) {
    if (line != null) {
        Text(
            text = line,
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .offset(y = 24.dp)
                    .padding(start = 40.dp),
            color = Color.White,
            fontSize = 15.sp,
            maxLines = 1,
        )
    } else {
        // The chevron sits centered at y≈1036 px, overlapping the card
        // row's bottom edge.
        Icon(
            painter = painterResource(R.drawable.ic_chevron_down),
            contentDescription = null,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 11.dp)
                    .size(28.dp),
            tint = Color.White.copy(alpha = 0.9f),
        )
    }
}
