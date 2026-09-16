package com.johncorser.telly.features.recording

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.johncorser.telly.R
import com.johncorser.telly.core.design.TELLY_ONBOARDING_BACKGROUND
import com.johncorser.telly.core.ui.TellyScreenEmptyState
import com.johncorser.telly.core.ui.TellyScreenWhiteText
import com.johncorser.telly.core.ui.rememberFocusSeed

/**
 * The library's title header + the row list (or the "No recordings" empty
 * state). Split from [RecordingsScreen] so the host stays a thin wiring
 * shell. The header mirrors the History screen's top-right title idiom.
 */
@Composable
internal fun RecordingsScreenContent(
    rows: List<RecordingRow>,
    onClick: (RecordingRow) -> Unit,
    onLongClick: (RecordingRow) -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(TELLY_ONBOARDING_BACKGROUND)),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, end = 24.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TellyScreenWhiteText(text = stringResource(R.string.recordings_title), fontSize = 28.sp)
        }
        if (rows.isEmpty()) {
            TellyScreenEmptyState(stringResource(R.string.recordings_empty))
        } else {
            val seed = rememberFocusSeed()
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(start = 40.dp, top = 80.dp, end = 40.dp)
                    .then(seed.modifier()),
            ) {
                items(rows, key = { it.entry.id }) { row ->
                    RecordingsScreenRow(
                        row = row,
                        requestFocus = row == rows.first(),
                        grabYielded = seed.seeded,
                        onClick = { onClick(row) },
                        onLongClick = { onLongClick(row) },
                    )
                }
            }
        }
    }
}
