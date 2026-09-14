package com.johncorser.telly.features.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_CLOCK_BLUE
import com.johncorser.telly.core.design.TELLY_NOW_LINE
import com.johncorser.telly.core.design.TELLY_PANE_DIVIDER
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED

/**
 * Header row (uidump 24, y≈408-464): blue date+clock over the channel
 * column, 30-min timeline labels panning with the grid, a dot marking the
 * now-line's head, and the thin divider underneath.
 */
@Composable
internal fun GuideScreenHeader(controller: GuideController) {
    val scrollX by controller.scrollX.collectAsState()
    Column {
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .height(27.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.width(GuideGeometry.CHANNEL_COLUMN_DP.dp)) {
                Text(
                    text = controller.clockText,
                    modifier = Modifier.padding(start = 16.dp),
                    color = Color(TELLY_CLOCK_BLUE),
                    fontSize = 14.sp,
                )
            }
            GuideScreenTimelineTicks(
                controller = controller,
                scrollX = scrollX,
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxHeight(),
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(TELLY_PANE_DIVIDER)),
        )
        Spacer(Modifier.height(2.dp))
    }
}

@Composable
private fun GuideScreenTimelineTicks(
    controller: GuideController,
    scrollX: Float,
    modifier: Modifier = Modifier,
) {
    val viewport = GuideGeometry.TIME_VIEWPORT_DP
    // Labels are centered on their half-hour tick (uidump 24: each 160 dp
    // slot straddles the tick, the first hanging into the channel column).
    val halfSlot = GuideGeometry.DP_PER_30_MIN / 2
    Box(modifier) {
        GuideTimeline.ticks(controller.originMs, scrollX, viewport, controller.zone).forEach { tick ->
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
        GuideTimeline.nowLineOffset(controller.nowMs, controller.originMs, scrollX, viewport)?.let { nowX ->
            Box(
                Modifier
                    .offset(x = nowX.dp)
                    .align(Alignment.BottomStart)
                    .size(5.dp)
                    .background(Color(TELLY_NOW_LINE), CircleShape),
            )
        }
    }
}
