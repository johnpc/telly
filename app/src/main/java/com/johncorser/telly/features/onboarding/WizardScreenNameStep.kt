package com.johncorser.telly.features.onboarding

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.johncorser.telly.R

/** The processed step (screen 12): editable playlist name pre-filled from
 * the URL host plus the TV/VOD radio choice; Next persists and finishes. */
@Composable
fun WizardScreenNameStep(
    name: String,
    kind: PlaylistKind,
    onNameChange: (String) -> Unit,
    onKindChange: (PlaylistKind) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val edit = rememberWizardScreenEditState { true }
    Row(modifier) {
        WizardScreenActionsPane(Modifier.weight(1f)) {
            if (edit.editing) {
                WizardScreenFieldEditor(
                    label = stringResource(R.string.wizard_playlist_name),
                    value = name,
                    onValueChange = onNameChange,
                    onCommit = edit::commit,
                    modifier = Modifier.width(WizardScreenDims.actionWidth),
                )
            } else {
                WizardScreenActionRow(
                    text = stringResource(R.string.wizard_playlist_name),
                    onClick = edit::open,
                    modifier =
                        Modifier
                            .width(WizardScreenDims.actionWidth)
                            .focusRequester(edit.rowFocus),
                    secondaryText = name.ifBlank { null },
                )
            }
            WizardScreenRadioRow(R.string.wizard_tv_playlist, kind == PlaylistKind.TV) {
                onKindChange(PlaylistKind.TV)
            }
            WizardScreenRadioRow(R.string.wizard_vod_playlist, kind == PlaylistKind.VOD) {
                onKindChange(PlaylistKind.VOD)
            }
        }
        WizardScreenButtonsPane {
            WizardScreenNextBackButtons(
                onNext = onNext,
                onBack = onBack,
                nextEnabled = true,
                nextFocus = edit.nextFocus,
            )
        }
    }
}

/** A radio-style guided action row (TV playlist / VOD playlist). */
@Composable
private fun WizardScreenRadioRow(
    label: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val icon = if (selected) R.drawable.ic_wizard_radio_on else R.drawable.ic_wizard_radio_off
    WizardScreenActionRow(
        text = stringResource(label),
        onClick = onClick,
        modifier = Modifier.width(WizardScreenDims.actionWidth),
        leadingIcon = painterResource(icon),
    )
}
