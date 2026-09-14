package com.johncorser.telly.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.johncorser.telly.core.design.TELLY_FIELD_UNDERLINE
import com.johncorser.telly.core.design.TELLY_TEXT_PRIMARY
import com.johncorser.telly.core.ui.LocalAccentColor

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
    val focusRequester = remember { FocusRequester() }
    // Retry across a couple of frames: the overlay mounts a frame after the
    // sheet, so a single requestFocus can land before the node is placed.
    LaunchedEffect(Unit) {
        repeat(3) {
            runCatching { focusRequester.requestFocus() }
            withFrameNanos { }
        }
    }
    Column {
        BasicTextField(
            value = value,
            onValueChange = { value = it },
            singleLine = true,
            textStyle =
                TextStyle(
                    color = Color(TELLY_TEXT_PRIMARY),
                    fontSize = SettingsScreenDims.titleSize,
                    fontFamily = SettingsScreenDims.fontFamily,
                ),
            cursorBrush = SolidColor(LocalAccentColor.current),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onCommit(value.text) }),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .padding(vertical = 8.dp),
        )
        Column(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(TELLY_FIELD_UNDERLINE)),
        ) {}
    }
}
