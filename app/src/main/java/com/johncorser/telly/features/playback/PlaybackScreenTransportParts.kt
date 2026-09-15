package com.johncorser.telly.features.playback

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED
import com.johncorser.telly.core.ui.FocusScreenDefaults
import com.johncorser.telly.features.catchup.CatchupSkip

/** The transport's catch-up controls (null during live playback). */
internal data class TransportCatchup(
    val paused: Boolean,
    val skip: CatchupSkip,
    val onPause: () -> Unit,
    val onSeek: (Long) -> Unit,
    val onPrevious: () -> Unit,
    val onNext: () -> Unit,
)

/** The transport record dot's state + the instant-record toggle. */
internal data class TransportRecord(
    val recording: Boolean = false,
    val onToggle: () -> Unit = {},
)

/**
 * The transport's record dot: red while the CURRENT channel is being
 * recorded, the resting grey ring otherwise — idle-looking but clickable
 * either way (start instant record / offer Stop), mirroring TiviMate's
 * transport record button.
 */
@Composable
internal fun TransportRecordDot(record: TransportRecord) {
    Surface(
        onClick = record.onToggle,
        modifier =
            Modifier
                .size(40.dp)
                .testTag("transport-record")
                .semantics { contentDescription = if (record.recording) "Recording" else "Record" },
        shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
        scale = FocusScreenDefaults.scale(),
        colors = FocusScreenDefaults.colors(restingContainer = Color.Transparent),
    ) {
        Box(
            Modifier
                .align(Alignment.Center)
                .size(14.dp)
                .then(
                    if (record.recording) {
                        Modifier.background(Color(REC_DOT_RED), CircleShape)
                    } else {
                        Modifier.border(4.dp, Color(TELLY_TEXT_MUTED), CircleShape)
                    },
                ),
        )
    }
}

/** The REC red the recordings library's badge uses. */
private const val REC_DOT_RED = 0xFFE53935
