package com.johncorser.telly.features.settings

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.johncorser.telly.core.ui.rememberAutoFocus

/**
 * The four-digit picker wheel row shared by the settings "Change PIN"
 * dialog and the panel's locked-group prompt: UP/DOWN spins the active
 * digit, LEFT/RIGHT moves the cursor, OK commits. Grabs focus on entry and
 * consumes its keys so the surfaces behind it never see them.
 */
@Composable
internal fun SettingsScreenPinWheel(
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val entry = remember { mutableStateOf(PinEntry()) }
    val focusRequester = rememberAutoFocus()
    Row(
        modifier =
            modifier
                .testTag("pin-wheel")
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { event ->
                    handleWheelKey(event, entry.value, onSubmit) { entry.value = it }
                },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        entry.value.digits.forEachIndexed { index, digit ->
            SettingsScreenPinDigit(digit = digit, active = index == entry.value.cursor)
        }
    }
}

/** Acts on key-up, but consumes the down events too (focus must not move). */
private fun handleWheelKey(
    event: KeyEvent,
    entry: PinEntry,
    onSubmit: (String) -> Unit,
    update: (PinEntry) -> Unit,
): Boolean {
    val handledKeys = setOf(Key.DirectionUp, Key.DirectionDown, Key.DirectionLeft, Key.DirectionRight)
    val isCommit = event.key == Key.DirectionCenter || event.key == Key.Enter
    if (event.key !in handledKeys && !isCommit) return false
    if (event.type == KeyEventType.KeyUp) {
        when (event.key) {
            Key.DirectionUp -> update(entry.up())
            Key.DirectionDown -> update(entry.down())
            Key.DirectionLeft -> update(entry.left())
            Key.DirectionRight -> update(entry.right())
            else -> update(entry.commit(onSubmit))
        }
    }
    return true
}
