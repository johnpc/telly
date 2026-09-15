package com.johncorser.telly.features.onboarding

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import com.johncorser.telly.R

/** The EPG step (screen 13): the url-tvg pre-filled into Enter URL, paste
 * helpers, the greyed "Use default source", and Done / Back actions. */
@Composable
fun WizardScreenEpgStep(
    epgUrl: String,
    error: WizardError?,
    onEpgUrlChange: (String) -> Unit,
    onPastePlaylistUrl: () -> Unit,
    onDone: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val edit = rememberWizardScreenEditState { true }
    Row(modifier) {
        WizardScreenActionsPane(Modifier.weight(1f)) {
            WizardScreenUrlField(
                label = stringResource(R.string.wizard_enter_url),
                value = epgUrl,
                edit = edit,
                onValueChange = onEpgUrlChange,
            )
            error?.let { WizardScreenUrlError(it) }
            WizardScreenClipboardRow(onEpgUrlChange)
            WizardScreenActionRow(
                text = stringResource(R.string.wizard_paste_playlist_url),
                onClick = onPastePlaylistUrl,
                modifier = Modifier.width(WizardScreenDims.actionWidth),
                secondaryText = stringResource(R.string.wizard_paste_playlist_hint),
            )
            // Greyed in the reference capture; telly has no bundled default.
            WizardScreenActionRow(
                text = stringResource(R.string.wizard_use_default_source),
                onClick = {},
                modifier = Modifier.width(WizardScreenDims.actionWidth),
                enabled = false,
            )
        }
        WizardScreenButtonsPane {
            WizardScreenActionRow(
                text = stringResource(R.string.wizard_done),
                onClick = onDone,
                modifier =
                    Modifier
                        .width(WizardScreenDims.buttonWidth)
                        .focusRequester(edit.nextFocus),
            )
            WizardScreenActionRow(
                text = stringResource(R.string.wizard_back),
                onClick = onBack,
                modifier = Modifier.width(WizardScreenDims.buttonWidth),
            )
        }
    }
}
