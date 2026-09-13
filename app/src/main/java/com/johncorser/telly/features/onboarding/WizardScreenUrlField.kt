package com.johncorser.telly.features.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.R
import com.johncorser.telly.core.design.TELLY_FIELD_UNDERLINE
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED
import com.johncorser.telly.core.design.TELLY_TEXT_PRIMARY

/** Inline URL editor (screens 08/09): label above an underlined text field. */
@Composable
fun WizardScreenUrlField(
    url: String,
    onUrlChange: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fieldFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { fieldFocus.requestFocus() }
    Column(modifier.padding(start = 14.dp, top = 11.dp)) {
        Text(
            text = stringResource(R.string.wizard_enter_url),
            color = Color(TELLY_TEXT_PRIMARY),
            fontSize = 16.sp,
        )
        Spacer(Modifier.height(16.dp))
        BasicTextField(
            value = url,
            onValueChange = onUrlChange,
            modifier =
                Modifier
                    .width(262.dp)
                    .focusRequester(fieldFocus),
            textStyle = TextStyle(color = Color(TELLY_TEXT_MUTED), fontSize = 16.sp),
            cursorBrush = SolidColor(Color(TELLY_TEXT_PRIMARY)),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier =
                Modifier
                    .width(262.dp)
                    .height(1.dp)
                    .background(Color(TELLY_FIELD_UNDERLINE)),
        )
    }
}
