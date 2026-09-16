package com.johncorser.telly.features.onboarding

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.johncorser.telly.R
import com.johncorser.telly.core.ui.focusOnAppear

/** Step 1 (screen 03): pick the playlist type; only M3U advances this slice. */
@Composable
fun WizardScreenTypeStep(
    onChoose: (PlaylistType) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier) {
        WizardScreenActionsPane(Modifier.weight(1f)) {
            WizardScreenActionRow(
                text = stringResource(R.string.wizard_m3u_playlist),
                onClick = { onChoose(PlaylistType.M3U) },
                modifier =
                    Modifier
                        .width(WizardScreenDims.actionWidth)
                        .focusOnAppear(),
            )
            WizardScreenActionRow(
                text = stringResource(R.string.wizard_xtream_codes),
                onClick = { onChoose(PlaylistType.XTREAM_CODES) },
                modifier = Modifier.width(WizardScreenDims.actionWidth),
            )
            WizardScreenActionRow(
                text = stringResource(R.string.wizard_stalker_portal),
                onClick = { onChoose(PlaylistType.STALKER_PORTAL) },
                modifier = Modifier.width(WizardScreenDims.actionWidth),
            )
        }
        WizardScreenButtonsPane {
            WizardScreenActionRow(
                text = stringResource(R.string.wizard_cancel),
                onClick = onCancel,
                modifier = Modifier.width(WizardScreenDims.buttonWidth),
            )
        }
    }
}
