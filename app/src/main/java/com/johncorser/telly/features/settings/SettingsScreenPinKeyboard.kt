package com.johncorser.telly.features.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.johncorser.telly.core.design.TELLY_TEXT_PRIMARY
import com.johncorser.telly.core.ui.focusOnAppear

/**
 * Routes every PIN prompt through the persisted "PIN input method": the
 * digit wheels ("Picker", the captured default) or the masked 4-digit IME
 * entry ("Keyboard"). Same verify semantics either way.
 */
@Composable
internal fun SettingsScreenPinEntry(
    keyboard: Boolean,
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (keyboard) {
        SettingsScreenPinKeyboard(onSubmit, modifier)
    } else {
        SettingsScreenPinWheel(onSubmit, modifier)
    }
}

/**
 * The "Keyboard" PIN input method: a masked digits-only text field that
 * commits on the fourth digit (or IME Done). The reference dialog is
 * premium-locked and uncapturable — minimal matching style.
 */
@Composable
internal fun SettingsScreenPinKeyboard(
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var value by remember { mutableStateOf(TextFieldValue("")) }
    SettingsScreenUnderlinedField(
        value = value,
        onValueChange = { raw ->
            // Commit clears the field so a rejected full entry can retry.
            val digits = PinKeyboard.sanitize(raw.text)
            val next = if (PinKeyboard.isComplete(digits)) PinKeyboard.commit(digits, onSubmit) else digits
            value = TextFieldValue(next, TextRange(next.length))
        },
        onDone = { text ->
            val next = PinKeyboard.commit(text, onSubmit)
            value = TextFieldValue(next, TextRange(next.length))
        },
        spec =
            EditorFieldSpec(
                textStyle =
                    TextStyle(
                        color = Color(TELLY_TEXT_PRIMARY),
                        fontSize = 24.sp,
                        letterSpacing = 8.sp,
                        fontFamily = SettingsScreenDims.fontFamily,
                    ),
                fieldModifier =
                    Modifier
                        .testTag("pin-keyboard")
                        // Placement-gated sticky grab: an eager single-shot
                        // request can land before the sheet is placed and
                        // crash the focus system's bring-into-view.
                        .focusOnAppear()
                        .width(PIN_FIELD_WIDTH)
                        .padding(vertical = 8.dp),
                fieldWidth = PIN_FIELD_WIDTH,
                keyboardType = KeyboardType.NumberPassword,
                visualTransformation = PasswordVisualTransformation(),
            ),
        modifier = modifier,
    )
}

private val PIN_FIELD_WIDTH = 152.dp
