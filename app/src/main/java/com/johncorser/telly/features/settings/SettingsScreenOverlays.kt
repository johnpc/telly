package com.johncorser.telly.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.johncorser.telly.core.design.TELLY_GUIDANCE_PANE
import com.johncorser.telly.features.reminders.ReminderScreenConfirmDelete
import com.johncorser.telly.features.vod.VodSettingsScreenConfirm

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
        is SettingsOverlay.ConfirmDeleteSource -> SettingsScreenConfirmDeleteSource(model, overlay)
        is SettingsOverlay.ConfirmDeleteReminder -> ReminderScreenConfirmDelete(model, overlay)
        SettingsOverlay.ConfirmClearVodPositions -> VodSettingsScreenConfirm(model)
        SettingsOverlay.ConfirmDeleteRecordings -> SettingsScreenConfirmDeleteRecordings(model)
        SettingsOverlay.PinSetup -> SettingsScreenPinDialog(title = "Change PIN", onSubmit = model::submitPin)
        SettingsOverlay.PinVerify -> SettingsScreenPinDialog(title = "Enter PIN", onSubmit = model::submitVerifyPin)
        SettingsOverlay.ConfirmClearHistory -> SettingsScreenConfirmClearHistory(model)
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
        SettingsScreenChoiceList(
            options = picker.spec.options,
            current = picker.current,
            onChoose = model::choosePickerOption,
        )
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
