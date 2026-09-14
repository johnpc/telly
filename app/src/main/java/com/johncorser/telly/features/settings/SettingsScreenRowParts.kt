package com.johncorser.telly.features.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Text
import com.johncorser.telly.R
import com.johncorser.telly.core.design.TELLY_FOCUS_TEXT
import com.johncorser.telly.core.design.TELLY_TEXT_DISABLED
import com.johncorser.telly.core.design.TELLY_TEXT_FAINT_FOCUSED
import com.johncorser.telly.core.design.TELLY_TEXT_GUIDANCE_MUTED
import com.johncorser.telly.core.ui.LocalAccentColor

/** Padlock, Unlock-Premium key, or playlist circle-check; else nothing. */
@Composable
internal fun SettingsScreenRowIcon(
    row: SettingsRow,
    locked: Boolean,
) {
    val premiumKey = (row as? SettingsRow.Action)?.premiumKey == true
    val check = (row as? SettingsRow.Value)?.checkIcon == true
    if (!locked && !premiumKey && !check) return
    val res =
        when {
            premiumKey -> R.drawable.ic_settings_key
            check -> R.drawable.ic_settings_check_circle
            else -> R.drawable.ic_settings_lock
        }
    // The circle-check is accent blue at rest and follows the near-black
    // content color under the focus pill (ref/05 vs ref/10).
    val focused = LocalContentColor.current == Color(TELLY_FOCUS_TEXT)
    val tint =
        when {
            locked -> Color(TELLY_TEXT_DISABLED)
            check && !focused -> LocalAccentColor.current
            else -> LocalContentColor.current
        }
    Box(Modifier.size(SettingsScreenDims.iconSize), contentAlignment = Alignment.Center) {
        Icon(
            painter = painterResource(res),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(if (check) 24.dp else 20.dp),
        )
    }
    Spacer(Modifier.width(SettingsScreenDims.iconTextGap))
}

@Composable
internal fun SettingsScreenRowSummary(
    row: SettingsRow,
    locked: Boolean,
) {
    val summary = (row as? SettingsRow.Value)?.summary ?: (row as? SettingsRow.Toggle)?.summary ?: return
    val focused = LocalContentColor.current == Color(TELLY_FOCUS_TEXT)
    val color =
        when {
            locked -> TELLY_TEXT_DISABLED
            focused -> TELLY_TEXT_FAINT_FOCUSED
            else -> TELLY_TEXT_GUIDANCE_MUTED
        }
    Text(
        text = summary,
        fontSize = SettingsScreenDims.summarySize,
        fontFamily = SettingsScreenDims.fontFamily,
        color = Color(color),
    )
}

/** Switch for toggles, blue check for selected value rows. */
@Composable
internal fun SettingsScreenRowTrailing(
    row: SettingsRow,
    locked: Boolean,
) {
    when {
        row is SettingsRow.Toggle -> SettingsScreenSwitch(checked = row.checked, locked = locked)
        // Trailing accent check on the selected picker option (ref/09b).
        row is SettingsRow.Value && row.selected ->
            Icon(
                painter = painterResource(R.drawable.ic_settings_check),
                contentDescription = null,
                tint = LocalAccentColor.current,
                modifier = Modifier.size(SettingsScreenDims.iconSize),
            )
    }
}
