package com.johncorser.telly.features.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.johncorser.telly.R
import com.johncorser.telly.core.design.TELLY_PANE_DIVIDER
import com.johncorser.telly.core.ui.FocusScreenReclaim

/** The wizard's middle column: action rows starting 189 dp from the top. */
@Composable
fun WizardScreenActionsPane(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxHeight()
                .padding(start = WizardScreenDims.panePadding, top = WizardScreenDims.paneTop),
        verticalArrangement = Arrangement.spacedBy(WizardScreenDims.rowSpacing),
        content = content,
    )
}

/** The wizard's right column (Next/Back/Cancel) behind a 1 dp divider. */
@Composable
fun WizardScreenButtonsPane(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(Color(TELLY_PANE_DIVIDER)),
    )
    Column(
        modifier =
            Modifier
                .width(WizardScreenDims.buttonPaneWidth)
                .fillMaxHeight()
                .padding(start = WizardScreenDims.panePadding, top = WizardScreenDims.paneTop),
        verticalArrangement = Arrangement.spacedBy(WizardScreenDims.rowSpacing),
        content = content,
    )
}

/** The shared Next (with -> glyph) over Back button pair (screens 07-12). */
@Composable
fun WizardScreenNextBackButtons(
    onNext: () -> Unit,
    onBack: () -> Unit,
    nextEnabled: Boolean,
    nextFocus: FocusScreenReclaim,
) {
    WizardScreenActionRow(
        text = stringResource(R.string.wizard_next),
        onClick = onNext,
        modifier =
            Modifier
                .width(WizardScreenDims.buttonWidth)
                .then(nextFocus.target()),
        enabled = nextEnabled,
        trailingIcon = painterResource(R.drawable.ic_wizard_next),
    )
    WizardScreenActionRow(
        text = stringResource(R.string.wizard_back),
        onClick = onBack,
        modifier = Modifier.width(WizardScreenDims.buttonWidth),
    )
}
