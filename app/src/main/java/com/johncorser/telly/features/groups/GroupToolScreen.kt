package com.johncorser.telly.features.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.johncorser.telly.features.settings.SettingsScreenPinEntry
import com.johncorser.telly.features.settings.SettingsScreenRows
import com.johncorser.telly.features.settings.SettingsScreenSheet
import com.johncorser.telly.features.settings.SettingsScreenTextField

/**
 * The one renderer of every group/bulk tool screen ([GroupToolUi]): a
 * settings-style right sheet whose body is the tool's row list, the name
 * editor, or the parental PIN wheel — shared verbatim by the guide's and
 * the playback panel's context sheets.
 */
@Composable
fun GroupToolScreen(session: GroupToolSession) {
    val ui by session.ui.collectAsState()
    SettingsScreenSheet(title = ui.title) {
        Box(Modifier.testTag("group-tool")) {
            when {
                ui.pin -> GroupToolScreenPin(keyboard = session.keyboardPin, onSubmit = session::submitPin)
                ui.textInitial != null ->
                    Column(Modifier.padding(24.dp)) {
                        SettingsScreenTextField(initial = ui.textInitial.orEmpty(), onCommit = session::submitText)
                    }
                else ->
                    SettingsScreenRows(
                        rows = ui.rows,
                        onActivate = session::activate,
                        initialFocusId = ui.focusId,
                    )
            }
        }
    }
}

/** The blocking editor's entry gate: the shared PIN prompt in the sheet. */
@Composable
private fun GroupToolScreenPin(
    keyboard: Boolean,
    onSubmit: (String) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsScreenPinEntry(keyboard = keyboard, onSubmit = onSubmit)
    }
}
