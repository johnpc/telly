package com.johncorser.telly.features.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_FIELD_UNDERLINE
import com.johncorser.telly.core.design.TELLY_TEXT_PRIMARY

/**
 * Inline editor for a wizard row (reference 08): white label above an
 * underlined grey field. The input is a real EditText — like leanback's —
 * so the TV IME anchors to the field and shows the Next (->|) action key.
 */
@Composable
fun WizardScreenFieldEditor(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onCommit: () -> Unit,
    modifier: Modifier = Modifier,
    uriInput: Boolean = false,
) {
    Column(modifier.padding(start = 14.dp, top = 14.dp, bottom = 11.dp)) {
        Text(
            text = label,
            color = Color(TELLY_TEXT_PRIMARY),
            fontSize = 14.sp,
            lineHeight = 19.sp,
            letterSpacing = 0.sp,
            fontFamily = WizardScreenDims.actionFontFamily,
        )
        Spacer(Modifier.height(2.dp))
        AndroidView(
            modifier =
                Modifier
                    .padding(start = WizardScreenDims.editorUnderlineInset)
                    .width(WizardScreenDims.editorWidth),
            factory = { context -> wizardScreenEditText(context, value, uriInput, onValueChange, onCommit) },
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier =
                Modifier
                    .padding(start = WizardScreenDims.editorUnderlineInset)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(TELLY_FIELD_UNDERLINE)),
        )
        Spacer(Modifier.height(10.dp))
    }
}
