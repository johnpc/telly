package com.johncorser.telly.features.multiview

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_CLOCK_BLUE
import com.johncorser.telly.core.ui.TellyScreenMutedText
import com.johncorser.telly.core.ui.TellyScreenProgramTitle
import com.johncorser.telly.core.ui.TellyScreenTimesLine
import com.johncorser.telly.features.panel.PanelRow

/** Read-only time + programme rows of the focused channel (capture 05). */
@Composable
internal fun MultiviewScreenPickerSchedule(
    schedule: List<MultiviewScheduleRow>,
    modifier: Modifier,
) {
    LazyColumn(modifier.semantics { contentDescription = "Multiview schedule" }) {
        items(schedule) { row ->
            Row(Modifier.height(SCHEDULE_ROW_HEIGHT), verticalAlignment = Alignment.CenterVertically) {
                TellyScreenMutedText(row.timeText)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = row.title,
                    color = if (row.airing) Color(TELLY_CLOCK_BLUE) else Color.White.copy(alpha = 0.9f),
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** The focused channel's airing programme, top-right (capture 05). */
@Composable
internal fun MultiviewScreenPickerDetail(row: PanelRow) {
    TellyScreenProgramTitle(row.nowTitle, fontSize = 20.sp)
    Spacer(Modifier.height(8.dp))
    TellyScreenTimesLine(range = row.nowRange, permille = row.progressPermille, remaining = row.remaining)
    row.description?.let {
        Spacer(Modifier.height(8.dp))
        TellyScreenMutedText(it, fontSize = 14.sp, maxLines = 4)
    }
}

private val SCHEDULE_ROW_HEIGHT = 37.dp
