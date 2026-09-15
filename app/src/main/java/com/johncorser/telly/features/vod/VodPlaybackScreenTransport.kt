package com.johncorser.telly.features.vod

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import com.johncorser.telly.R
import com.johncorser.telly.core.ui.TellyScreenMutedText
import com.johncorser.telly.core.ui.TellyScreenProgramTitle
import com.johncorser.telly.core.ui.TellyScreenProgressBar
import com.johncorser.telly.core.ui.TellyScreenWhiteText

/**
 * The VOD seek transport (ux-spec §VOD): title over a thumbed progress
 * line with position/duration clocks, on a dim band at the screen bottom
 * (the info-overlay idiom; the reference's VOD transport is uncaptured).
 */
@Composable
internal fun VodPlaybackScreenTransport(
    viewModel: VodPlaybackViewModel,
    modifier: Modifier = Modifier,
) {
    val item by viewModel.item.collectAsState()
    val progress by viewModel.controls.progress.collectAsState()
    val paused by viewModel.controls.paused.collectAsState()
    Column(
        modifier
            .testTag("vod-transport")
            .fillMaxWidth()
            .background(Color(TRANSPORT_SCRIM))
            .padding(horizontal = 40.dp, vertical = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (paused) {
                Icon(
                    painter = painterResource(R.drawable.ic_tr_pause),
                    contentDescription = "Paused",
                    modifier =
                        Modifier
                            .padding(end = 10.dp)
                            .size(20.dp),
                    tint = Color.White,
                )
            }
            TellyScreenProgramTitle(title = item?.name)
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TellyScreenWhiteText(text = VodTimes.format(progress.positionMs), fontSize = 15.sp)
            TellyScreenProgressBar(
                permille = progress.permille,
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp),
                thumb = true,
            )
            TellyScreenMutedText(text = VodTimes.format(progress.durationMs), fontSize = 15.sp)
        }
    }
}

private const val TRANSPORT_SCRIM = 0xB3000000
