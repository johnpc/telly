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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.johncorser.telly.core.design.TELLY_ONBOARDING_BACKGROUND
import com.johncorser.telly.core.ui.focusOnAppear
import com.johncorser.telly.core.ui.rememberFocusSeed

/**
 * A full-screen GuidedStep in the reference style (screens 22/28): left
 * guidance pane with icon + 36 sp title + body lines, action pills right.
 */
@OptIn(ExperimentalComposeUiApi::class)
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
            .background(Color(TELLY_ONBOARDING_BACKGROUND))
            // D-pad stays on the action pills — the surfaces behind the
            // full-screen step must not catch it. BACK arrives as a focus
            // EXIT, which must stay unconsumed so it reaches the back
            // dispatcher (cancelling it eats the key and strands the step).
            .focusProperties {
                exit = { direction ->
                    if (direction == FocusDirection.Exit) FocusRequester.Default else FocusRequester.Cancel
                }
            },
    ) {
        SettingsScreenGuidedPane(iconRes = iconRes, title = title, bodyLines = bodyLines)
        val seed = rememberFocusSeed()
        Column(
            Modifier
                .fillMaxHeight()
                .padding(start = 20.dp, top = 189.dp)
                .then(seed.modifier()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            actions.forEachIndexed { index, (label, onClick) ->
                SettingsScreenRow(
                    row = SettingsRow.Action(id = "guided:$label", title = label),
                    onActivate = { onClick() },
                    modifier =
                        Modifier
                            .width(328.dp)
                            .focusOnAppear(enabled = index == 0, yielded = seed.seeded),
                    resting = Color.Transparent,
                )
            }
        }
    }
}
