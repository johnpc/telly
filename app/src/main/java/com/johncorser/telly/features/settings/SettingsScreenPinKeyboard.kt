package com.johncorser.telly.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.johncorser.telly.core.design.TELLY_FIELD_UNDERLINE
import com.johncorser.telly.core.design.TELLY_TEXT_PRIMARY
import com.johncorser.telly.core.ui.LocalAccentColor
import com.johncorser.telly.core.ui.rememberAutoFocus

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
    var value by remember { mutableStateOf("") }
    Column(modifier) {
        BasicTextField(
            value = value,
            onValueChange = { raw ->
                val digits = PinKeyboard.sanitize(raw)
                value = if (PinKeyboard.isComplete(digits)) PinKeyboard.commit(digits, onSubmit) else digits
            },
            singleLine = true,
            textStyle =
                TextStyle(
                    color = Color(TELLY_TEXT_PRIMARY),
                    fontSize = 24.sp,
                    letterSpacing = 8.sp,
                    fontFamily = SettingsScreenDims.fontFamily,
                ),
            visualTransformation = PasswordVisualTransformation(),
            cursorBrush = SolidColor(LocalAccentColor.current),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { value = PinKeyboard.commit(value, onSubmit) }),
            modifier =
                Modifier
                    .testTag("pin-keyboard")
                    .focusRequester(rememberAutoFocus())
                    .width(PIN_FIELD_WIDTH)
                    .padding(vertical = 8.dp),
        )
        Column(
            Modifier
                .width(PIN_FIELD_WIDTH)
                .height(1.dp)
                .background(Color(TELLY_FIELD_UNDERLINE)),
        ) {}
    }
}

private val PIN_FIELD_WIDTH = 152.dp
