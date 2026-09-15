package com.johncorser.telly.features.playback

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED
import com.johncorser.telly.core.ui.TellyScreenIconCircle
import com.johncorser.telly.core.ui.rememberAutoFocus

/**
 * Transport row revealed by a second UP in the info overlay (round3-ref
 * 03b): programme elapsed/duration left, ⏮ ⏪ ⏸ ⏩ ⏭ center, LIVE badge and
 * record dot right. During catch-up ([TransportCatchup] non-null) the row
 * is the seek transport: ⏮/⏭ hop programmes, RW/FF seek, ⏸ pauses/resumes
 * (icon reflects state), the readout is position/duration (adapted by
 * CatchupInfo) and the LIVE badge disappears; live playback keeps every
 * center button on the coming-soon pattern (live pause = timeshift, a
 * later slice). The record dot is live either way (ux-spec §1.1).
 */
@Composable
internal fun PlaybackScreenTransportRow(
    data: PlaybackInfoData,
    onFeature: (String) -> Unit,
    catchup: TransportCatchup? = null,
    record: TransportRecord = TransportRecord(),
) {
    val firstFocus = rememberAutoFocus()
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 8.dp)
            .then(if (catchup != null) Modifier.testTag("catchup-transport") else Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = data.elapsed.orEmpty(), color = Color.White, fontSize = 15.sp)
        Text(text = " / ${data.duration.orEmpty()}", color = Color(TELLY_TEXT_MUTED), fontSize = 15.sp)
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            transportButtons.forEachIndexed { index, button ->
                TellyScreenIconCircle(
                    icon = transportIcon(index, button.icon, catchup),
                    onClick = { transportAction(index, button.feature, onFeature, catchup) },
                    modifier = if (index == PAUSE_INDEX) Modifier.focusRequester(firstFocus) else Modifier,
                    contentDescription = transportLabel(index, button.feature, catchup),
                )
            }
        }
        Spacer(Modifier.weight(1f))
        if (catchup == null) {
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
        TransportRecordDot(record)
    }
}
