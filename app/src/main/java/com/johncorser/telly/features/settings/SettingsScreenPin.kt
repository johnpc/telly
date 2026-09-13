package com.johncorser.telly.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_BUTTON_RESTING
import com.johncorser.telly.core.design.TELLY_TEXT_PRIMARY
import com.johncorser.telly.core.ui.FocusScreenDefaults
import com.johncorser.telly.core.ui.LocalAccentColor

/**
 * Picker-style PIN entry (PIN input method "Picker"): four digit wheels,
 * UP/DOWN spins, LEFT/RIGHT moves, OK commits. The reference dialog is
 * premium-locked and uncapturable — minimal matching style, VERIFY-ON-DEVICE.
 */
@Composable
internal fun SettingsScreenPinDialog(model: SettingsViewModel) {
    var entry by remember { mutableStateOf(PinEntry()) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    SettingsScreenSheet(title = "Change PIN") {
        Row(
            modifier =
                Modifier
                    .padding(SettingsScreenDims.panePadding)
                    .focusRequester(focusRequester)
                    .focusable()
                    .onPreviewKeyEvent { event ->
                        if (event.type != KeyEventType.KeyUp) return@onPreviewKeyEvent false
                        when (event.key) {
                            Key.DirectionUp -> true.also { entry = entry.up() }
                            Key.DirectionDown -> true.also { entry = entry.down() }
                            Key.DirectionLeft -> true.also { entry = entry.left() }
                            Key.DirectionRight -> true.also { entry = entry.right() }
                            Key.DirectionCenter, Key.Enter -> true.also { model.submitPin(entry.value) }
                            else -> false
                        }
                    },
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            entry.digits.forEachIndexed { index, digit ->
                SettingsScreenPinDigit(digit = digit, active = index == entry.cursor)
            }
        }
    }
}

@Composable
private fun SettingsScreenPinDigit(
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
                    shape =
                        androidx.compose.foundation.shape
                            .RoundedCornerShape(FocusScreenDefaults.cornerRadius),
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
