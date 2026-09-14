package com.johncorser.telly.features.playback

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.R
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED
import com.johncorser.telly.core.ui.TellyScreenIconCircle
import com.johncorser.telly.core.ui.rememberAutoFocus

/**
 * Transport row revealed by a second UP in the info overlay (round3-ref
 * 03b): programme elapsed/duration left, ⏮ ⏪ ⏸ ⏩ ⏭ center (timeshift is a
 * later slice — every button routes to the coming-soon pattern), LIVE badge
 * and record dot right.
 */
@Composable
internal fun PlaybackScreenTransportRow(
    data: PlaybackInfoData,
    onFeature: (String) -> Unit,
) {
    val firstFocus = rememberAutoFocus()
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = data.elapsed.orEmpty(), color = Color.White, fontSize = 15.sp)
        Text(text = " / ${data.duration.orEmpty()}", color = Color(TELLY_TEXT_MUTED), fontSize = 15.sp)
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            transportButtons.forEachIndexed { index, (icon, feature) ->
                TellyScreenIconCircle(
                    icon = icon,
                    onClick = { onFeature(feature) },
                    modifier = if (index == PAUSE_INDEX) Modifier.focusRequester(firstFocus) else Modifier,
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = "LIVE",
            modifier =
                Modifier
                    .border(1.dp, Color.White, RoundedCornerShape(3.dp))
                    .padding(horizontal = 5.dp, vertical = 1.dp),
            color = Color.White,
            fontSize = 12.sp,
        )
        Spacer(Modifier.width(16.dp))
        Box(
            Modifier
                .size(14.dp)
                .border(4.dp, Color(TELLY_TEXT_MUTED), CircleShape),
        )
    }
}

private const val PAUSE_INDEX = 2

private val transportButtons =
    listOf(
        R.drawable.ic_tr_prev to "Previous programme",
        R.drawable.ic_tr_rewind to "Timeshift",
        R.drawable.ic_tr_pause to "Timeshift",
        R.drawable.ic_tr_forward to "Timeshift",
        R.drawable.ic_tr_next to "Next programme",
    )
