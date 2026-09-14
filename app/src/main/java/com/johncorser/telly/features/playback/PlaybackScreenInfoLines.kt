package com.johncorser.telly.features.playback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.johncorser.telly.core.design.TELLY_BADGE_TEXT
import com.johncorser.telly.core.ui.TellyScreenMutedText
import com.johncorser.telly.core.ui.TellyScreenProgramTitle
import com.johncorser.telly.core.ui.TellyScreenTimesLine

/**
 * Programme title, times/number/badges line, optional description (zap
 * variant) and the next-programme line — shared by both overlay variants.
 */
@Composable
internal fun PlaybackScreenInfoLines(
    data: PlaybackInfoData,
    showBadges: Boolean,
    showDescription: Boolean,
) {
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
            if (showBadges) PlaybackScreenBadges(data.badges)
        }
        if (showDescription) {
            Spacer(Modifier.height(6.dp))
            data.description?.let { TellyScreenMutedText(it, fontSize = 14.sp) }
        }
        Spacer(Modifier.height(6.dp))
        data.nextLine?.let { TellyScreenMutedText(it) }
    }
}

/** Translucent dark pills, grey caps text, 8 dp apart (round3 item 10). */
@Composable
private fun PlaybackScreenBadges(badges: List<String>) {
    Row(Modifier.padding(start = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        badges.forEach { label ->
            Text(
                text = label,
                modifier =
                    Modifier
                        .background(Color(TELLY_BADGE_FILL), RoundedCornerShape(3.dp))
                        .padding(horizontal = 3.dp, vertical = 1.dp),
                color = Color(TELLY_BADGE_TEXT),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
