package com.johncorser.telly.features.recording

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Text
import com.johncorser.telly.core.ui.TellyScreenChannelRow
import com.johncorser.telly.core.ui.focusOnAppear

/**
 * One Recordings-library row in the app's 39 dp channel-row idiom: title
 * (with a live REC badge while capturing), channel, start stamp and the
 * status detail. OK plays/stops/cancels per status; long-OK deletes.
 */
@Composable
internal fun RecordingsScreenRow(
    row: RecordingRow,
    requestFocus: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    TellyScreenChannelRow(
        tag = "recording-row",
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = Modifier.focusOnAppear(requestFocus),
    ) {
        if (row.status == RecordingStatus.RECORDING) {
            RecordingsScreenRecBadge()
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = row.entry.title,
            modifier = Modifier.weight(1f),
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = row.entry.channelName,
            modifier = Modifier.width(150.dp),
            color = LocalContentColor.current.copy(alpha = 0.7f),
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = row.dateText,
            modifier = Modifier.width(170.dp),
            color = LocalContentColor.current.copy(alpha = 0.7f),
            fontSize = 13.sp,
            maxLines = 1,
        )
        Text(
            text = row.detailText,
            color = LocalContentColor.current.copy(alpha = 0.7f),
            fontSize = 13.sp,
            maxLines = 1,
        )
    }
}

/** The red REC pill on in-progress rows. */
@Composable
private fun RecordingsScreenRecBadge() {
    Text(
        text = "REC",
        modifier =
            Modifier
                .background(Color(REC_RED), RoundedCornerShape(2.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp),
        color = Color.White,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
    )
}

private const val REC_RED = 0xFFE53935
