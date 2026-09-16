package com.johncorser.telly.features.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.johncorser.telly.core.design.TELLY_PANE_DIVIDER

/**
 * Header row (uidump 24, y≈408-464): blue date+clock over the channel
 * column, 30-min timeline labels panning with the grid, a dot marking the
 * now-line's head, and the thin divider underneath.
 */
@Composable
internal fun GuideScreenHeader(controller: GuideController) {
    val scrollX by controller.scrollX.collectAsState()
    val now by controller.now.collectAsState()
    Column {
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .height(27.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GuideScreenClock(nowMs = now, style = controller.clockStyle)
            GuideScreenTimelineTicks(
                controller = controller,
                scrollX = scrollX,
                nowMs = now,
                modifier = Modifier.weight(1f),
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
