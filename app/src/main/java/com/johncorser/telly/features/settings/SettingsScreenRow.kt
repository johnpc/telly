package com.johncorser.telly.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_GUIDANCE_PANE
import com.johncorser.telly.core.design.TELLY_TEXT_DISABLED
import com.johncorser.telly.core.ui.FocusScreenDefaults
import com.johncorser.telly.core.ui.focusOnAppear

/** One interactive settings row: focus pill, optional icon/summary/widget. */
@Composable
internal fun SettingsScreenRow(
    row: SettingsRow,
    onActivate: (String) -> Unit,
    modifier: Modifier = Modifier,
    onFocused: () -> Unit = {},
    requestFocus: Boolean = false,
    resting: Color = Color(TELLY_GUIDANCE_PANE),
) {
    val locked = row.isLocked()
    Surface(
        onClick = { onActivate(row.id) },
        enabled = !locked,
        modifier =
            modifier
                .focusOnAppear(requestFocus)
                .fillMaxWidth()
                .heightIn(min = SettingsScreenDims.rowMinHeight)
                .onFocusChanged { if (it.isFocused) onFocused() }
                .focusProperties { canFocus = !locked },
        shape = FocusScreenDefaults.shape(),
        scale = FocusScreenDefaults.scale(),
        colors = FocusScreenDefaults.colors(restingContainer = resting),
    ) {
        Row(
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = SettingsScreenDims.rowPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SettingsScreenRowIcon(row, locked)
            // Text pads 12 dp vertically (ref rows: title top = row + 24 px);
            // taller neighbors (icon, switch) stay centered on the row.
            Column(
                Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = row.titleText(),
                    fontSize = SettingsScreenDims.titleSize,
                    lineHeight = SettingsScreenDims.titleLineHeight,
                    fontFamily = SettingsScreenDims.fontFamily,
                    color = if (locked) Color(TELLY_TEXT_DISABLED) else LocalContentColor.current,
                )
                SettingsScreenRowSummary(row, locked)
            }
            SettingsScreenRowTrailing(row, locked)
        }
    }
}

internal fun SettingsRow.isLocked(): Boolean =
    when (this) {
        is SettingsRow.Toggle -> locked
        is SettingsRow.Value -> locked
        is SettingsRow.Action -> locked
        else -> false
    }

internal fun SettingsRow.titleText(): String =
    when (this) {
        is SettingsRow.Toggle -> title
        is SettingsRow.Value -> title
        is SettingsRow.Action -> title
        is SettingsRow.Header -> text
        is SettingsRow.Note -> text
    }
