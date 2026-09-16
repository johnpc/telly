package com.johncorser.telly.features.playback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.R
import com.johncorser.telly.core.ui.TellyScreenIconCircle
import com.johncorser.telly.core.ui.focusOnAppear
import com.johncorser.telly.core.ui.rememberFocusSeed

/**
 * Bottom icon quick-bar from long-OK / MENU at fullscreen (round3-ref
 * 07/08): nine evenly spread icon+label slots over a bottom scrim, focus a
 * white circle starting on Search, top scrim with group + clock above.
 */
@Composable
internal fun PlaybackScreenQuickBar(viewModel: PlaybackViewModel) {
    val info by viewModel.info.collectAsState()
    val seed = rememberFocusSeed()
    Box(Modifier.fillMaxSize()) {
        PlaybackScreenTopScrim(info?.group, info?.clockText.orEmpty(), Modifier.align(Alignment.TopCenter))
        Row(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .then(seed.modifier()),
            verticalAlignment = Alignment.Bottom,
        ) {
            viewModel.quickBarItems().forEachIndexed { index, item ->
                PlaybackScreenQuickBarSlot(
                    item = item,
                    onClick = { viewModel.onQuickBarItem(item.action) },
                    modifier = Modifier.weight(1f),
                    requestFocus = index == 0,
                    grabYielded = seed.seeded,
                )
            }
        }
    }
}

@Composable
private fun PlaybackScreenQuickBarSlot(
    item: QuickBarItem,
    onClick: () -> Unit,
    modifier: Modifier,
    requestFocus: Boolean,
    grabYielded: () -> Boolean,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        TellyScreenIconCircle(
            icon = quickBarIcon(item.action),
            onClick = onClick,
            modifier = Modifier.focusOnAppear(requestFocus, grabYielded),
            contentDescription = item.action.feature,
        )
        Spacer(Modifier.height(6.dp))
        Text(text = item.label, color = Color.White, fontSize = 12.sp, maxLines = 1)
    }
}

private fun quickBarIcon(action: QuickBarAction): Int =
    when (action) {
        QuickBarAction.SEARCH -> R.drawable.ic_menu_search
        QuickBarAction.CHANNELS_LIST -> R.drawable.ic_qb_list
        QuickBarAction.RECORDINGS -> R.drawable.ic_qb_recordings
        QuickBarAction.MULTIVIEW -> R.drawable.ic_qb_multiview
        QuickBarAction.PICTURE_IN_PICTURE -> R.drawable.ic_qb_pip
        QuickBarAction.RESOLUTION -> R.drawable.ic_qb_resolution
        QuickBarAction.AUDIO -> R.drawable.ic_qb_audio
        QuickBarAction.LATENCY -> R.drawable.ic_qb_latency
        QuickBarAction.SUBTITLES -> R.drawable.ic_qb_cc
    }
