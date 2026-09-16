package com.johncorser.telly.features.playback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.johncorser.telly.core.design.TELLY_BADGE_FILL
import com.johncorser.telly.core.ui.TellyScreenWhiteText

/**
 * Transient "Reconnecting…" pill shown while [PlayerState.Reconnecting] — a
 * dropped live stream is being re-prepared with exponential backoff. Sits
 * top-start over the frozen last frame; disappears the moment the stream
 * resumes ([PlayerState.Playing]) or the retry budget runs out ([Error]).
 */
@Composable
internal fun PlaybackScreenReconnecting() {
    Box(Modifier, contentAlignment = Alignment.TopStart) {
        Box(
            Modifier
                .padding(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(TELLY_BADGE_FILL))
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            TellyScreenWhiteText("Reconnecting…", fontSize = 15.sp)
        }
    }
}
