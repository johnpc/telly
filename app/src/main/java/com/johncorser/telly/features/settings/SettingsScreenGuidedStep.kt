package com.johncorser.telly.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.johncorser.telly.core.design.TELLY_ONBOARDING_BACKGROUND
import com.johncorser.telly.core.ui.focusOnAppear

/**
 * A full-screen GuidedStep in the reference style (screens 22/28): left
 * guidance pane with icon + 36 sp title + body lines, action pills right.
 */
@Composable
internal fun SettingsScreenGuidedStep(
    iconRes: Int,
    title: String,
    bodyLines: List<String>,
    actions: List<Pair<String, () -> Unit>>,
) {
    Row(
        Modifier
            .fillMaxSize()
            .background(Color(TELLY_ONBOARDING_BACKGROUND)),
    ) {
        SettingsScreenGuidedPane(iconRes = iconRes, title = title, bodyLines = bodyLines)
        Column(
            Modifier
                .fillMaxHeight()
                .padding(start = 20.dp, top = 189.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            actions.forEachIndexed { index, (label, onClick) ->
                SettingsScreenRow(
                    row = SettingsRow.Action(id = "guided:$label", title = label),
                    onActivate = { onClick() },
                    modifier =
                        Modifier
                            .width(298.dp)
                            .focusOnAppear(enabled = index == 0),
                    resting = Color.Transparent,
                )
            }
        }
    }
}
