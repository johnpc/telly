package com.johncorser.telly.features.onboarding

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import com.johncorser.telly.R

/** Step 2 (screens 07-10): enter the playlist URL, then Next to load it.
 * Committing the inline editor with ENTER moves focus to Next (ref 10). */
@Composable
fun WizardScreenUrlStep(
    url: String,
    error: WizardError?,
    onUrlChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val edit = rememberWizardScreenEditState { url.isNotBlank() }
    Row(modifier) {
        WizardScreenActionsPane(Modifier.weight(1f)) {
            if (edit.editing) {
                WizardScreenFieldEditor(
                    label = stringResource(R.string.wizard_enter_url),
                    value = url,
                    onValueChange = onUrlChange,
                    onCommit = edit::commit,
                    modifier = Modifier.width(WizardScreenDims.actionWidth),
                    uriInput = true,
                )
            } else {
                WizardScreenActionRow(
                    text = stringResource(R.string.wizard_enter_url),
                    onClick = edit::open,
                    modifier =
                        Modifier
                            .width(WizardScreenDims.actionWidth)
                            .focusRequester(edit.rowFocus),
                    secondaryText = url.ifBlank { null },
                )
            }
            error?.let { WizardScreenUrlError(it) }
            WizardScreenClipboardRow(onUrlChange)
            WizardScreenActionRow(
                text = stringResource(R.string.wizard_select_local_playlist),
                onClick = {},
                modifier = Modifier.width(WizardScreenDims.actionWidth),
            )
        }
        WizardScreenButtonsPane {
            WizardScreenNextBackButtons(
                onNext = onNext,
                onBack = onBack,
                nextEnabled = url.isNotBlank(),
                nextFocus = edit.nextFocus,
            )
        }
    }
}
