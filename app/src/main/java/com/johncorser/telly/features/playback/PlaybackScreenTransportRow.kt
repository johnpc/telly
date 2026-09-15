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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.R
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED
import com.johncorser.telly.core.ui.TellyScreenIconCircle
import com.johncorser.telly.core.ui.rememberAutoFocus
import com.johncorser.telly.features.catchup.CatchupSkip

/**
 * Transport row revealed by a second UP in the info overlay (round3-ref
 * 03b): programme elapsed/duration left, ⏮ ⏪ ⏸ ⏩ ⏭ center, LIVE badge and
 * record dot right. During catch-up ([onSeek] non-null) the row is the seek
 * transport: RW/FF buttons seek, the readout is position/duration (already
 * adapted by CatchupInfo) and the LIVE badge disappears; live playback keeps
 * every button on the coming-soon pattern (timeshift is a later slice).
 */
@Composable
internal fun PlaybackScreenTransportRow(
    data: PlaybackInfoData,
    onFeature: (String) -> Unit,
    onSeek: ((Long) -> Unit)? = null,
    skip: CatchupSkip = CatchupSkip(),
) {
    val firstFocus = rememberAutoFocus()
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 8.dp)
            .then(if (onSeek != null) Modifier.testTag("catchup-transport") else Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = data.elapsed.orEmpty(), color = Color.White, fontSize = 15.sp)
        Text(text = " / ${data.duration.orEmpty()}", color = Color(TELLY_TEXT_MUTED), fontSize = 15.sp)
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            transportButtons.forEachIndexed { index, (icon, feature) ->
                TellyScreenIconCircle(
                    icon = icon,
                    onClick = { transportAction(index, feature, onFeature, onSeek, skip) },
                    modifier = if (index == PAUSE_INDEX) Modifier.focusRequester(firstFocus) else Modifier,
                )
            }
        }
        Spacer(Modifier.weight(1f))
        if (onSeek == null) {
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
        }
        Box(
            Modifier
                .size(14.dp)
                .border(4.dp, Color(TELLY_TEXT_MUTED), CircleShape),
        )
    }
}

/** RW/FF seek during catch-up; everything else keeps its live routing. */
private fun transportAction(
    index: Int,
    feature: String,
    onFeature: (String) -> Unit,
    onSeek: ((Long) -> Unit)?,
    skip: CatchupSkip,
) = when {
    onSeek != null && index == REWIND_INDEX -> onSeek(-skip.backMs)
    onSeek != null && index == FORWARD_INDEX -> onSeek(skip.forwardMs)
    else -> onFeature(feature)
}

private const val REWIND_INDEX = 1
private const val PAUSE_INDEX = 2
private const val FORWARD_INDEX = 3

private val transportButtons =
    listOf(
        R.drawable.ic_tr_prev to "Previous programme",
        R.drawable.ic_tr_rewind to "Timeshift",
        R.drawable.ic_tr_pause to "Timeshift",
        R.drawable.ic_tr_forward to "Timeshift",
        R.drawable.ic_tr_next to "Next programme",
    )
