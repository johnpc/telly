package com.johncorser.telly.features.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.johncorser.telly.core.design.TELLY_TEXT_PRIMARY
import com.johncorser.telly.core.ui.focusOnAppear

/**
 * Underlined single-line editor for the value dialogs, styled after the
 * wizard's inline field (reference 08). Commit = IME Done. The exact
 * reference dialogs are premium-locked: VERIFY-ON-DEVICE.
 */
@Composable
internal fun SettingsScreenTextField(
    initial: String,
    onCommit: (String) -> Unit,
) {
    var value by remember { mutableStateOf(TextFieldValue(initial, TextRange(initial.length))) }
    SettingsScreenUnderlinedField(
        value = value,
        onValueChange = { value = it },
        onDone = onCommit,
        spec =
            EditorFieldSpec(
                textStyle =
                    TextStyle(
                        color = Color(TELLY_TEXT_PRIMARY),
                        fontSize = SettingsScreenDims.titleSize,
                        fontFamily = SettingsScreenDims.fontFamily,
                    ),
                fieldModifier =
                    Modifier
                        .fillMaxWidth()
                        // The sticky grab defers to placement — the overlay
                        // mounts a frame after the sheet, and an eager grab
                        // on the unplaced field crashes bring-into-view.
                        .focusOnAppear()
                        .padding(vertical = 8.dp),
            ),
    )
}
