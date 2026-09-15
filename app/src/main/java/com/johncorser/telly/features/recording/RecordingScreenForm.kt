package com.johncorser.telly.features.recording

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_MENU_SHEET
import com.johncorser.telly.core.ui.TellyScreenMenuRow
import com.johncorser.telly.features.playback.ProgramTimes
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
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .width(420.dp)
                .background(Color(TELLY_MENU_SHEET), RoundedCornerShape(4.dp))
                .padding(vertical = 16.dp, horizontal = 8.dp),
        ) {
            Text(
                text = "Custom recording",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = Color.White,
                fontSize = 18.sp,
            )
            RecordingScreenFormValue(label = "Channel", value = form.channel.source.name)
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
}
