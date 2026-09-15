package com.johncorser.telly.features.playback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.johncorser.telly.R
import com.johncorser.telly.core.ui.rememberAutoFocus

/**
 * The shortcut row (history-round2 §1): TV guide · History · Multiview ·
 * one card per recently watched channel · Clear (only with recent cards).
 * TV guide takes focus (capture 34); the row scrolls when history outgrows
 * the screen. Multiview is telly's addition (see PlaybackViewModel.openMultiview).
 */
@Composable
internal fun PlaybackScreenCards(
    recents: List<RecentCard>,
    actions: PlaybackCardActions,
    onRecentFocus: (RecentCard, Boolean) -> Unit,
) {
    val firstFocus = rememberAutoFocus()
    // Clear unmounts the focused card row tail; focus falls back to TV guide.
    LaunchedEffect(recents.isEmpty()) {
        if (recents.isEmpty()) runCatching { firstFocus.requestFocus() }
    }
    LazyRow(
        contentPadding = PaddingValues(start = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item {
            PlaybackScreenCard(
                label = stringResource(R.string.playback_card_tv_guide),
                icon = R.drawable.ic_card_guide,
                onClick = actions.onGuide,
                modifier = Modifier.focusRequester(firstFocus),
            )
        }
        item {
            PlaybackScreenCard(
                label = stringResource(R.string.playback_card_history),
                icon = R.drawable.ic_card_history,
                onClick = actions.onHistory,
            )
        }
        item {
            PlaybackScreenCard(
                label = stringResource(R.string.playback_card_multiview),
                icon = R.drawable.ic_qb_multiview,
                onClick = actions.onMultiview,
            )
        }
        items(recents, key = { it.channel.id }) { card ->
            PlaybackScreenRecentCard(card, onClick = { actions.onRecent(card) }, onFocus = onRecentFocus)
        }
        if (recents.isNotEmpty()) {
            item {
                PlaybackScreenCard(
                    label = stringResource(R.string.playback_card_clear),
                    icon = R.drawable.ic_search_trash,
                    onClick = actions.onClear,
                )
            }
        }
    }
}

/** The row's activation callbacks, bundled so hosts stay readable. */
internal class PlaybackCardActions(
    val onGuide: () -> Unit,
    val onHistory: () -> Unit,
    val onMultiview: () -> Unit,
    val onRecent: (RecentCard) -> Unit,
    val onClear: () -> Unit,
)
