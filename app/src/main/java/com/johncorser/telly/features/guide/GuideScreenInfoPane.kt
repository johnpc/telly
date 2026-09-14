package com.johncorser.telly.features.guide

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.johncorser.telly.core.ui.TellyScreenMutedText
import com.johncorser.telly.core.ui.TellyScreenProgramTitle
import com.johncorser.telly.core.ui.TellyScreenTimesLine
import com.johncorser.telly.core.ui.TellyScreenWhiteText

/**
 * The focused programme's info pane at the guide's top right (uidump 24):
 * title, times line with the group name, description, and a star (favorite)
 * marker on the far right.
 */
@Composable
internal fun GuideScreenInfoPane(
    info: GuideInfoData?,
    modifier: Modifier = Modifier,
) {
    Row(modifier.padding(start = 12.dp, top = 4.dp)) {
        val data = info ?: return
        Column(Modifier.weight(1f)) {
            TellyScreenProgramTitle(data.title)
            Spacer(Modifier.height(6.dp))
            TellyScreenTimesLine(
                range = data.range,
                permille = data.progressPermille ?: 0,
                remaining = data.remaining,
            ) {
                data.group?.let { TellyScreenWhiteText(it) }
            }
            Spacer(Modifier.height(8.dp))
            data.description?.let { TellyScreenMutedText(it, fontSize = 14.sp, maxLines = 5) }
        }
        Spacer(Modifier.width(12.dp))
        TellyScreenWhiteText(if (data.favorite) "★" else "☆", fontSize = 18.sp)
    }
}
