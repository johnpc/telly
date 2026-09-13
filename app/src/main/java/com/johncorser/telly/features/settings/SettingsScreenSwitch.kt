package com.johncorser.telly.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.johncorser.telly.core.design.TELLY_TEXT_DISABLED
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED
import com.johncorser.telly.core.ui.LocalAccentColor

/**
 * The Material-2 style switch TiviMate renders (uidump: 94x54 px = 47x27 dp
 * touch box, pill track + round thumb; accent when on, grey when off,
 * dimmed when the row is premium-locked). Purely visual — the row handles
 * the click.
 */
@Composable
internal fun SettingsScreenSwitch(
    checked: Boolean,
    locked: Boolean,
) {
    val accent = LocalAccentColor.current
    val thumbColor =
        when {
            locked -> Color(TELLY_TEXT_DISABLED)
            checked -> accent
            else -> Color(TELLY_TEXT_MUTED)
        }
    val trackColor = if (checked && !locked) accent.copy(alpha = 0.5f) else thumbColor.copy(alpha = 0.4f)
    Box(
        modifier = Modifier.size(SettingsScreenDims.switchWidth, SettingsScreenDims.switchHeight),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(SettingsScreenDims.switchTrackWidth, SettingsScreenDims.switchTrackHeight)
                .background(trackColor, RoundedCornerShape(percent = 50)),
        )
        Box(
            Modifier
                .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
                .size(SettingsScreenDims.switchThumb)
                .background(thumbColor, CircleShape),
        )
    }
}
