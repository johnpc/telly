package com.johncorser.telly.features.playback

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.R
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED
import com.johncorser.telly.core.ui.LocalAccentColor
import com.johncorser.telly.core.ui.TellyScreenLogoTile
import com.johncorser.telly.core.ui.TellyScreenProgressBar

/**
 * Bottom info overlay (capture 34): top scrim with group + clock, channel
 * logo, programme lines, full-width progress bar and the shortcut cards.
 */
@Composable
internal fun PlaybackScreenInfoOverlay(viewModel: PlaybackViewModel) {
    val info by viewModel.info.collectAsState()
    val data = info ?: return
    val historyLabel = stringResource(R.string.playback_card_history)
    Box(Modifier.fillMaxSize()) {
        PlaybackScreenTopScrim(data.group, data.clockText, Modifier.align(Alignment.TopCenter))
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)))),
        ) {
            Row(Modifier.padding(horizontal = 40.dp), verticalAlignment = Alignment.CenterVertically) {
                TellyScreenLogoTile(
                    logoUrl = data.logoUrl,
                    name = data.name,
                    size = 79.dp,
                    modifier = Modifier.border(2.dp, LocalAccentColor.current, RoundedCornerShape(4.dp)),
                )
                Spacer(Modifier.width(16.dp))
                PlaybackScreenInfoLines(data)
            }
            Spacer(Modifier.height(12.dp))
            TellyScreenProgressBar(
                permille = data.progressPermille,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp),
                thumb = true,
            )
            Spacer(Modifier.height(12.dp))
            PlaybackScreenCards(
                onGuide = { viewModel.openPanel() },
                onHistory = { viewModel.showComingSoon(historyLabel) },
            )
            Text(
                text = "⌄",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = Color(TELLY_TEXT_MUTED),
                fontSize = 16.sp,
            )
        }
    }
}

/** Top scrim strip: group name left, date + clock right (capture 34). */
@Composable
private fun PlaybackScreenTopScrim(
    group: String?,
    clockText: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)))
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(text = group.orEmpty(), color = Color.White, fontSize = 16.sp)
        Spacer(Modifier.weight(1f))
        Text(text = clockText, color = Color.White, fontSize = 16.sp)
    }
}
