package com.johncorser.telly.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.johncorser.telly.core.design.TELLY_FIELD_UNDERLINE
import com.johncorser.telly.core.ui.LocalAccentColor

/**
 * The shared chrome of the underlined editors (the value dialog and the
 * keyboard PIN prompt): single-line accent-cursor field, IME-Done commit,
 * 1 dp underline. Look-and-feel knobs ride in [EditorFieldSpec].
 */
@Composable
internal fun SettingsScreenUnderlinedField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onDone: (String) -> Unit,
    spec: EditorFieldSpec,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = spec.textStyle,
            visualTransformation = spec.visualTransformation,
            cursorBrush = SolidColor(LocalAccentColor.current),
            keyboardOptions = KeyboardOptions(keyboardType = spec.keyboardType, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone(value.text) }),
            modifier = spec.fieldModifier,
        )
        SettingsScreenFieldUnderline(spec.fieldWidth)
    }
}

/** One editor's look: text style, field modifier, width and IME shape. */
internal class EditorFieldSpec(
    val textStyle: TextStyle,
    val fieldModifier: Modifier,
    /** Underline width; null spans the full dialog width. */
    val fieldWidth: Dp? = null,
    val keyboardType: KeyboardType = KeyboardType.Text,
    val visualTransformation: VisualTransformation = VisualTransformation.None,
)

/** The 1 dp underline every underlined editor sits on. */
@Composable
internal fun SettingsScreenFieldUnderline(width: Dp? = null) {
    val widthModifier = width?.let { Modifier.width(it) } ?: Modifier.fillMaxWidth()
    Column(
        widthModifier
            .height(1.dp)
            .background(Color(TELLY_FIELD_UNDERLINE)),
    ) {}
}
