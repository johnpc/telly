package com.johncorser.telly.features.guide

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
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

/** Raw KeyEvents → [GuideKey]s, holds split by the per-key detectors. */
internal fun mapGuideKey(
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
