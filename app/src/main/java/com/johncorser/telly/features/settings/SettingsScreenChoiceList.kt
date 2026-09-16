package com.johncorser.telly.features.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * The shared single-choice option list (settings pickers + the Channel
 * options pane's decoder/EPG-offset pickers): a blue check on the current raw.
 */
@Composable
internal fun SettingsScreenChoiceList(
    options: List<PickerOption>,
    current: String,
    onChoose: (String) -> Unit,
) {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
    ) {
        items(options, key = { it.raw }) { option ->
            SettingsScreenRow(
                row =
                    SettingsRow.Value(
                        id = "picker:${option.raw}",
                        title = option.label,
                        selected = option.raw == current,
                    ),
                onActivate = { onChoose(option.raw) },
                modifier = Modifier.padding(horizontal = SettingsScreenDims.rowMargin),
            )
        }
    }
}
