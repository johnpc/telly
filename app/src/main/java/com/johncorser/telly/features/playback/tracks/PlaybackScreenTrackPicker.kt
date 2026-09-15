package com.johncorser.telly.features.playback.tracks

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
import com.johncorser.telly.R
import com.johncorser.telly.core.ui.TellyScreenMenuRow

/**
 * Compact centered picker dialog over playback (ux-spec §3.14): title, then
 * one focusable row per option with the active one radio-checked. The sync
 * picker is a stepper — its title carries the live offset and its rows stay
 * open while stepping; BACK dismisses (PlaybackKeyPolicy's Pushed handling).
 */
@Composable
internal fun PlaybackScreenTrackPicker(
    controller: TrackPickerController,
    kind: TrackPickerKind,
) {
    val snapshot by controller.tracks.snapshot.collectAsState()
    val offset by controller.tracks.audioOffsetMs.collectAsState()
    val rows = TrackPickerRows.of(kind, snapshot)
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .width(320.dp)
                .background(Color(0xF2161B21), RoundedCornerShape(8.dp))
                .padding(vertical = 14.dp, horizontal = 8.dp),
        ) {
            Text(
                text = if (kind == TrackPickerKind.SYNC) "${kind.title} · ${TrackLabels.sync(offset)}" else kind.title,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
            )
            rows.forEachIndexed { index, row ->
                TellyScreenMenuRow(
                    label = row.label,
                    onClick = { controller.onRow(kind, row.id) },
                    icon = pickerRowIcon(kind, row.checked),
                    requestFocus = index == 0,
                )
            }
        }
    }
}

/** Radio marks single-choice pickers; the sync stepper rows are plain actions. */
private fun pickerRowIcon(
    kind: TrackPickerKind,
    checked: Boolean,
): Int? =
    when {
        kind == TrackPickerKind.SYNC -> null
        checked -> R.drawable.ic_wizard_radio_on
        else -> R.drawable.ic_wizard_radio_off
    }
