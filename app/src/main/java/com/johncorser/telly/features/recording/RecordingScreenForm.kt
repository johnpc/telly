package com.johncorser.telly.features.recording

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.johncorser.telly.core.design.TELLY_MENU_SHEET
import com.johncorser.telly.core.ui.TellyScreenDialog
import com.johncorser.telly.core.ui.TellyScreenMenuRow
import com.johncorser.telly.features.playback.ProgramTimes
import com.johncorser.telly.features.playlist.db.displayName
import java.util.TimeZone

/**
 * The "Custom recording" form (recording slice): a small centered dialog —
 * channel prefilled from the invoking context, start time and duration
 * adjustable with LEFT/RIGHT (OK also steps forward), Create schedules it.
 * Both the guide and the playback hosts render this over their scrims.
 */
@Composable
fun RecordingScreenForm(
    menu: RecordingMenu,
    zone: TimeZone = TimeZone.getDefault(),
) {
    val open by menu.form.collectAsState()
    val form = open ?: return
    val startMs by form.startMs.collectAsState()
    val durationMinutes by form.durationMinutes.collectAsState()
    TellyScreenDialog(
        title = "Custom recording",
        width = 420.dp,
        fill = Color(TELLY_MENU_SHEET),
        corner = 4.dp,
        verticalPadding = 16.dp,
        titleSize = 18.sp,
        titlePadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
    ) {
        RecordingScreenFormValue(label = "Channel", value = form.channel.displayName)
        RecordingScreenFormStepperRow(
            label = "Start time",
            value = ProgramTimes.clock(startMs, zone),
            requestFocus = true,
            onAdjust = form::adjustStart,
        )
        RecordingScreenFormStepperRow(
            label = "Duration",
            value = "$durationMinutes min",
            onAdjust = form::adjustDuration,
        )
        TellyScreenMenuRow(label = "Create", onClick = menu::createFromForm, height = 40.dp)
    }
}
