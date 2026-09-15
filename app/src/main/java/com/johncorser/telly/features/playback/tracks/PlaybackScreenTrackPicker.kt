package com.johncorser.telly.features.playback.tracks

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.johncorser.telly.R
import com.johncorser.telly.core.ui.TellyScreenDialog
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
    TellyScreenDialog(
        title = if (kind == TrackPickerKind.SYNC) "${kind.title} · ${TrackLabels.sync(offset)}" else kind.title,
        width = 320.dp,
        fill = Color(0xF2161B21),
        corner = 8.dp,
        verticalPadding = 14.dp,
        titleSize = 16.sp,
        titlePadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 10.dp),
    ) {
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
