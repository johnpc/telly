package com.johncorser.telly.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_BUTTON_RESTING
import com.johncorser.telly.core.design.TELLY_TEXT_PRIMARY
import com.johncorser.telly.core.ui.FocusScreenDefaults
import com.johncorser.telly.core.ui.LocalAccentColor

/**
 * PIN setup/change dialog: the entry style follows the persisted "PIN input
 * method" (wheels or masked keyboard). The reference dialog is
 * premium-locked and uncapturable — minimal matching style, VERIFY-ON-DEVICE.
 */
@Composable
internal fun SettingsScreenPinDialog(model: SettingsViewModel) {
    SettingsScreenSheet(title = "Change PIN") {
        SettingsScreenPinEntry(
            keyboard = model.parental.usesKeyboardPin,
            onSubmit = model::submitPin,
            modifier = Modifier.padding(SettingsScreenDims.panePadding),
        )
    }
}

/**
 * The "Require PIN for Settings | Playlists" gate: verifying re-runs the
 * pending section open; a wrong PIN keeps prompting (the blocked-channels
 * precedent), BACK dismisses.
 */
@Composable
internal fun SettingsScreenPinVerify(model: SettingsViewModel) {
    SettingsScreenSheet(title = "Enter PIN") {
        SettingsScreenPinEntry(
            keyboard = model.parental.usesKeyboardPin,
            onSubmit = model::submitVerifyPin,
            modifier = Modifier.padding(SettingsScreenDims.panePadding),
        )
    }
}

/** One digit wheel of the picker-style PIN entry. */
@Composable
internal fun SettingsScreenPinDigit(
    digit: Int,
    active: Boolean,
) {
    val accent = LocalAccentColor.current
    Box(
        modifier =
            Modifier
                .size(44.dp, 56.dp)
                .background(
                    color = if (active) accent.copy(alpha = 0.25f) else Color(TELLY_BUTTON_RESTING),
                    shape = RoundedCornerShape(FocusScreenDefaults.cornerRadius),
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = digit.toString(),
            color = if (active) accent else Color(TELLY_TEXT_PRIMARY),
            fontSize = 24.sp,
            fontFamily = SettingsScreenDims.fontFamily,
        )
    }
}
