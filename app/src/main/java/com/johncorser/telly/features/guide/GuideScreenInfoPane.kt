package com.johncorser.telly.features.guide

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.johncorser.telly.core.ui.TellyScreenMutedText
import com.johncorser.telly.core.ui.TellyScreenProgramTitle
import com.johncorser.telly.core.ui.TellyScreenTimesLine
import com.johncorser.telly.core.ui.TellyScreenWhiteText

/**
 * The focused programme's info pane at the guide's top right (uidump 24):
 * 23 sp title with the star (favorite) marker on the far right, then the
 * times line with the group name right-aligned at the pane's edge under
 * the star, then the description.
 */
@Composable
internal fun GuideScreenInfoPane(
    info: GuideInfoData?,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(top = 6.dp)) {
        val data = info ?: return
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { TellyScreenProgramTitle(data.title, fontSize = 23.sp) }
            Spacer(Modifier.width(12.dp))
            TellyScreenWhiteText(if (data.favorite) "★" else "☆", fontSize = 20.sp)
        }
        Spacer(Modifier.height(6.dp))
        TellyScreenTimesLine(
            range = data.range,
            permille = data.progressPermille ?: 0,
            remaining = data.remaining,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Spacer(Modifier.weight(1f))
            data.group?.let { TellyScreenWhiteText(it, fontSize = 16.sp) }
        }
        Spacer(Modifier.height(8.dp))
        data.description?.let { TellyScreenMutedText(it, fontSize = 14.sp, maxLines = 5) }
    }
}
