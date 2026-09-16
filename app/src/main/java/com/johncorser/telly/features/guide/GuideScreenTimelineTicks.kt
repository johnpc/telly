package com.johncorser.telly.features.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_CLOCK_BLUE
import com.johncorser.telly.core.design.TELLY_NOW_LINE
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED
import com.johncorser.telly.features.playback.ClockStyle
import com.johncorser.telly.features.playback.ProgramTimes

/** Blue date+clock over the channel column (uidump 24); minute-ticked. */
@Composable
internal fun GuideScreenClock(
    nowMs: Long,
    style: ClockStyle,
) {
    Box(Modifier.width(GuideGeometry.CHANNEL_COLUMN_DP.dp)) {
        Text(
            text = ProgramTimes.clock(nowMs, style),
            modifier = Modifier.padding(start = 16.dp),
            color = Color(TELLY_CLOCK_BLUE),
            fontSize = 14.sp,
        )
    }
}

/**
 * The scrolling 30-min timeline labels and the dot heading the now-line
 * (uidump 24); "now" re-samples every minute and on foreground resume.
 */
@Composable
internal fun GuideScreenTimelineTicks(
    controller: GuideController,
    scrollX: Float,
    nowMs: Long,
    modifier: Modifier = Modifier,
) {
    val viewport = GuideGeometry.TIME_VIEWPORT_DP
    // Labels are centered on their half-hour tick (uidump 24: each 160 dp
    // slot straddles the tick, the first hanging into the channel column).
    val halfSlot = GuideGeometry.DP_PER_30_MIN / 2
    Box(modifier.fillMaxHeight()) {
        GuideTimeline.ticks(controller.originMs, scrollX, viewport, controller.clockStyle).forEach { tick ->
            Text(
                text = tick.label,
                modifier =
                    Modifier
                        .offset(x = (tick.offsetDp - halfSlot).dp)
                        .width(GuideGeometry.DP_PER_30_MIN.dp)
                        .align(Alignment.CenterStart),
                color = Color(TELLY_TEXT_MUTED),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
        }
        GuideTimeline.nowLineOffset(nowMs, controller.originMs, scrollX, viewport)?.let { nowX ->
            Box(
                Modifier
                    .offset(x = nowX.dp)
                    .align(Alignment.BottomStart)
                    .size(5.dp)
                    .testTag("now-line")
                    .background(Color(TELLY_NOW_LINE), CircleShape),
            )
        }
    }
}
