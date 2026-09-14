package com.johncorser.telly.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_DASH_FILL
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED

/**
 * The recurring "02:30 — 03:45 PM  ▬▬  61 min" line (captures 34/47), shared
 * by the info overlay and the panel's detail card; [trailing] appends the
 * context-specific tail (channel number + badges, or the group name). The
 * 40x3 dp mini dash fills light grey, not accent blue (round3 item 9).
 */
@Composable
fun TellyScreenTimesLine(
    range: String?,
    permille: Int,
    remaining: String?,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        range?.let { Text(text = it, color = Color(TELLY_TEXT_MUTED), fontSize = 15.sp) }
        TellyScreenProgressBar(
            permille = permille,
            modifier = Modifier.width(40.dp),
            fill = Color(TELLY_DASH_FILL),
            thickness = 3.dp,
        )
        remaining?.let { Text(text = it, color = Color(TELLY_TEXT_MUTED), fontSize = 15.sp) }
        trailing()
    }
}
