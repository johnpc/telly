package com.johncorser.telly.features.playback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_BADGE_FILL
import com.johncorser.telly.core.ui.TellyScreenMutedText
import com.johncorser.telly.core.ui.TellyScreenProgramTitle
import com.johncorser.telly.core.ui.TellyScreenTimesLine

/** Programme title, times/number/badges line and the next-programme line. */
@Composable
internal fun PlaybackScreenInfoLines(data: PlaybackInfoData) {
    Column {
        TellyScreenProgramTitle(data.title)
        Spacer(Modifier.height(6.dp))
        TellyScreenTimesLine(
            range = data.timeRange,
            permille = data.progressPermille,
            remaining = data.remaining,
        ) {
            Text(
                text = "${data.number}  ${data.name}",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
            data.badges.forEach { PlaybackScreenBadge(it) }
        }
        Spacer(Modifier.height(6.dp))
        data.nextLine?.let { TellyScreenMutedText(it) }
    }
}

/** Grey rounded pill with white caps text: HD / 25 FPS / MONO (capture 34). */
@Composable
private fun PlaybackScreenBadge(label: String) {
    Text(
        text = label,
        modifier =
            Modifier
                .background(Color(TELLY_BADGE_FILL), RoundedCornerShape(3.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
    )
}
