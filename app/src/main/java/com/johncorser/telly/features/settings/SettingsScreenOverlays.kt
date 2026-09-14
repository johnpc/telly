package com.johncorser.telly.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.johncorser.telly.core.design.TELLY_GUIDANCE_PANE

/** Routes the active overlay to its dialog; nothing when idle. */
@Composable
internal fun SettingsScreenOverlay(
    model: SettingsViewModel,
    overlay: SettingsOverlay?,
) {
    when (overlay) {
        null -> Unit
        is SettingsOverlay.Picker -> SettingsScreenPicker(model, overlay)
        is SettingsOverlay.TextEdit -> SettingsScreenTextEdit(model, overlay)
        is SettingsOverlay.ConfirmDelete -> SettingsScreenConfirmDelete(model, overlay)
        SettingsOverlay.PinSetup -> SettingsScreenPinDialog(model)
        SettingsOverlay.Paywall -> SettingsScreenPaywall(onClose = { model.dismissOverlay() })
    }
}

/** Dimming scrim + the 360 dp right sheet every settings surface uses. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun SettingsScreenSheet(
    title: String,
    content: @Composable () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(SCRIM)),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Box(
            Modifier
                .width(SettingsScreenDims.leftPaneWidth)
                .fillMaxHeight()
                .background(Color(TELLY_GUIDANCE_PANE)),
        ) {
            SettingsScreenPane(title = title) { content() }
        }
    }
}

/** Single-choice picker: option list with a blue check on the current one. */
@Composable
private fun SettingsScreenPicker(
    model: SettingsViewModel,
    picker: SettingsOverlay.Picker,
) {
    SettingsScreenSheet(title = picker.spec.title) {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp),
        ) {
            items(picker.spec.options, key = { it.raw }) { option ->
                SettingsScreenRow(
                    row =
                        SettingsRow.Value(
                            id = "picker:${option.raw}",
                            title = option.label,
                            selected = option.raw == picker.current,
                        ),
                    onActivate = { model.choosePickerOption(option.raw) },
                    modifier = Modifier.padding(horizontal = SettingsScreenDims.rowMargin),
                )
            }
        }
    }
}

/** Free-form value editor (User-Agent, UDP proxy, playlist rename). */
@Composable
private fun SettingsScreenTextEdit(
    model: SettingsViewModel,
    edit: SettingsOverlay.TextEdit,
) {
    SettingsScreenSheet(title = edit.title) {
        Column(Modifier.padding(SettingsScreenDims.panePadding)) {
            SettingsScreenTextField(initial = edit.value, onCommit = model::submitText)
        }
    }
}

// The underlay dims to ~41% of its brightness in the reference (uidump 25
// vs the live view sampled on-device), i.e. a 60%-black scrim.
private const val SCRIM = 0x99000000
