package com.johncorser.telly.features.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED
import com.johncorser.telly.core.ui.TellyScreenTimesLine

/**
 * Air-time line: "12:45 — 01:45 AM ▬▬ 50 min" while airing (live tm-03),
 * bare times otherwise — both at the caller's size (round7 P3: the shared
 * times line's default 15 sp rendered the rows' airing times a size up
 * from tm's ~13 sp).
 */
@Composable
internal fun SearchScreenAirTime(
    hit: SearchProgramHit,
    fontSize: TextUnit = 14.sp,
) {
    if (hit.remaining == null) {
        Text(text = hit.timeText, color = Color(TELLY_TEXT_MUTED), fontSize = fontSize, maxLines = 1)
    } else {
        TellyScreenTimesLine(
            range = hit.timeText,
            permille = hit.progressPermille,
            remaining = hit.remaining,
            fontSize = fontSize,
        )
    }
}
