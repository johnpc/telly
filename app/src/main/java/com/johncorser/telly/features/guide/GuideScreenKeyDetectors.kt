package com.johncorser.telly.features.guide

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import com.johncorser.telly.core.input.HoldKeyDetector

/**
 * Per-key hold detectors for the grid: LEFT/RIGHT holds become day jumps
 * (the hint toast's "Long Left: navigate to past programs"), an OK hold is
 * swallowed so a long press never mis-fires stage-one tuning.
 */
internal class GuideScreenKeyDetectors {
    val ok = HoldKeyDetector<GuideKey>(GuideKey.OK, hold = null)
    val left = HoldKeyDetector(GuideKey.LEFT, GuideKey.LONG_LEFT)
    val right = HoldKeyDetector(GuideKey.RIGHT, GuideKey.LONG_RIGHT)
}

/** Invisible focus holder owning the D-pad while the grid layer is active. */
@Composable
internal fun GuideScreenKeyAnchor(onKey: (GuideKey) -> Boolean) {
    val focusRequester = remember { FocusRequester() }
    val detectors = remember { GuideScreenKeyDetectors() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    Box(
        Modifier
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event -> mapGuideKey(event, detectors)?.let(onKey) ?: false },
    )
}

private fun mapGuideKey(
    event: KeyEvent,
    detectors: GuideScreenKeyDetectors,
): GuideKey? {
    val down = event.type == KeyEventType.KeyDown
    val repeat = event.nativeKeyEvent.repeatCount
    return when (event.key) {
        Key.DirectionCenter, Key.Enter -> detectors.ok.route(down, repeat)
        Key.DirectionLeft -> detectors.left.route(down, repeat)
        Key.DirectionRight -> detectors.right.route(down, repeat)
        Key.DirectionUp -> GuideKey.UP.takeIf { down }
        Key.DirectionDown -> GuideKey.DOWN.takeIf { down }
        else -> null
    }
}

private fun HoldKeyDetector<GuideKey>.route(
    down: Boolean,
    repeat: Int,
): GuideKey? = if (down) onDown(repeat) else onUp()
