package com.johncorser.telly.features.onboarding

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.R
import com.johncorser.telly.core.design.TELLY_ERROR_TEXT

/** The shared inline URL field: an action row that turns into the inline
 * editor on OK, returning focus per [WizardScreenEditState] on commit. */
@Composable
internal fun WizardScreenUrlField(
    label: String,
    value: String,
    edit: WizardScreenEditState,
    onValueChange: (String) -> Unit,
) {
    if (edit.editing) {
        WizardScreenFieldEditor(
            label = label,
            value = value,
            onValueChange = onValueChange,
            onCommit = edit::commit,
            modifier = Modifier.width(WizardScreenDims.actionWidth),
            uriInput = true,
        )
    } else {
        WizardScreenActionRow(
            text = label,
            onClick = edit::open,
            modifier =
                Modifier
                    .width(WizardScreenDims.actionWidth)
                    .focusRequester(edit.rowFocus),
            secondaryText = value.ifBlank { null },
        )
    }
}

/** "Paste from clipboard" row: copies the clipboard text into the URL draft. */
@Composable
internal fun WizardScreenClipboardRow(onUrlChange: (String) -> Unit) {
    val clipboard = LocalClipboardManager.current
    WizardScreenActionRow(
        text = stringResource(R.string.wizard_paste_from_clipboard),
        onClick = { clipboard.getText()?.let { onUrlChange(it.text) } },
        modifier = Modifier.width(WizardScreenDims.actionWidth),
    )
}

/** Inline validation/load error rendered under the URL row. */
@Composable
internal fun WizardScreenUrlError(error: WizardError) {
    val message =
        when (error) {
            WizardError.INVALID_URL -> R.string.wizard_error_invalid_url
            WizardError.LOAD_FAILED -> R.string.wizard_error_load_failed
        }
    Text(
        text = stringResource(message),
        color = Color(TELLY_ERROR_TEXT),
        fontSize = 14.sp,
        modifier =
            Modifier
                .width(WizardScreenDims.actionWidth)
                .padding(start = 14.dp),
    )
}
