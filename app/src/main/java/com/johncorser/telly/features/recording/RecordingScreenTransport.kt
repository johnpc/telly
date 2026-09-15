package com.johncorser.telly.features.recording

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_BADGE_FILL
import com.johncorser.telly.core.design.TELLY_PLAYBACK_ACCENT
import com.johncorser.telly.core.design.TELLY_PROGRESS_TRACK
import com.johncorser.telly.features.playback.ProgramTimes

/** Bottom transport bar of the capture player: title, times, progress. */
@Composable
internal fun RecordingScreenTransport(
    title: String,
    positionMs: Long,
    durationMs: Long,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(Color(TELLY_BADGE_FILL))
            .padding(horizontal = 40.dp, vertical = 16.dp),
    ) {
        Row(Modifier.fillMaxWidth()) {
            Text(text = title, color = Color.White, fontSize = 16.sp, maxLines = 1)
            Spacer(Modifier.weight(1f))
            Text(
                text = "${ProgramTimes.span(positionMs)} / ${ProgramTimes.span(durationMs)}",
                color = Color.White,
                fontSize = 14.sp,
            )
        }
        Spacer(Modifier.height(8.dp))
        val fraction = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
        Box(
            Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Color(TELLY_PROGRESS_TRACK), RoundedCornerShape(2.dp)),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction)
                    .height(4.dp)
                    .background(Color(TELLY_PLAYBACK_ACCENT), RoundedCornerShape(2.dp)),
            )
        }
    }
}

/** Capture-player D-pad map: LEFT/RIGHT seek ±10 s, OK toggles pause. */
internal fun onRecordingPlayerKey(
    event: KeyEvent,
    player: ExoPlayer,
): Boolean {
    if (event.type != KeyEventType.KeyDown) return false
    return when (event.key) {
        Key.DirectionLeft -> {
            player.seekTo((player.currentPosition - SEEK_STEP_MS).coerceAtLeast(0))
            true
        }
        Key.DirectionRight -> {
            player.seekTo(player.currentPosition + SEEK_STEP_MS)
            true
        }
        Key.DirectionCenter, Key.Enter -> {
            toggleRecordingPlayback(player)
            true
        }
        else -> false
    }
}

/**
 * OK: pause/resume — and at the capture's end (the player stops on its
 * last frame with the transport showing the full progress) it replays
 * from the start instead of toggling a dead playWhenReady flag.
 */
internal fun toggleRecordingPlayback(player: ExoPlayer) {
    if (player.playbackState == Player.STATE_ENDED) {
        player.seekTo(0)
        player.playWhenReady = true
    } else {
        player.playWhenReady = !player.playWhenReady
    }
}

private const val SEEK_STEP_MS = 10_000L
