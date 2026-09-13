package com.johncorser.telly.features.onboarding

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.johncorser.telly.R

/** Step 2 (screens 07-10): enter the playlist URL, then Next to load it. */
@Composable
fun WizardScreenUrlStep(
    url: String,
    error: WizardError?,
    onUrlChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var editing by remember { mutableStateOf(false) }
    val urlRowFocus = remember { FocusRequester() }
    LaunchedEffect(editing) { if (!editing) urlRowFocus.requestFocus() }
    Row(modifier) {
        WizardScreenActionsPane(Modifier.weight(1f)) {
            if (editing) {
                WizardScreenUrlField(
                    url = url,
                    onUrlChange = onUrlChange,
                    onDone = { editing = false },
                    modifier = Modifier.width(WizardScreenDims.actionWidth),
                )
            } else {
                WizardScreenActionRow(
                    text = stringResource(R.string.wizard_enter_url),
                    onClick = { editing = true },
                    modifier =
                        Modifier
                            .width(WizardScreenDims.actionWidth)
                            .focusRequester(urlRowFocus),
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
            WizardScreenActionRow(
                text = stringResource(R.string.wizard_next),
                onClick = onNext,
                modifier = Modifier.width(WizardScreenDims.buttonWidth),
                enabled = url.isNotBlank(),
                trailingIcon = painterResource(R.drawable.ic_wizard_next),
            )
            WizardScreenActionRow(
                text = stringResource(R.string.wizard_back),
                onClick = onBack,
                modifier = Modifier.width(WizardScreenDims.buttonWidth),
            )
        }
    }
}
