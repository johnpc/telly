package com.johncorser.telly.core.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceColors
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ClickableSurfaceScale
import androidx.tv.material3.ClickableSurfaceShape
import com.johncorser.telly.core.design.TELLY_FOCUS_FILL
import com.johncorser.telly.core.design.TELLY_FOCUS_TEXT
import com.johncorser.telly.core.design.TELLY_TEXT_DISABLED
import com.johncorser.telly.core.design.TELLY_TEXT_PRIMARY

/**
 * Grabs D-pad focus when the element enters composition and KEEPS asking
 * until the grab sticks (bounded, see [grabFocusUntilLanded]) — the shared
 * "focus me on appear" pattern of menus, cards and key anchors.
 */
@Composable
fun Modifier.focusOnAppear(enabled: Boolean = true): Modifier {
    val grab = rememberStickyFocusGrab()
    return if (enabled) then(grab) else this
}

/** The sticky grab as a standalone modifier: requester + bounded retry. */
@Composable
private fun rememberStickyFocusGrab(): Modifier {
    val requester = remember { FocusRequester() }
    val landed = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        grabFocusUntilLanded(
            landed = { landed.value },
            request = { requester.requestFocus() },
            awaitFrame = { withFrameNanos { } },
        )
    }
    return Modifier
        .focusRequester(requester)
        .onFocusChanged { if (it.hasFocus) landed.value = true }
}

/**
 * TiviMate's universal focus treatment (reference screens 02/03/07): a light
 * rounded pill with near-black content, no scale-up and no glow.
 */
object FocusScreenDefaults {
    /** 8 px corner radius in the 1920x1080 reference = 4 dp at xhdpi. */
    val cornerRadius = 4.dp

    @Composable
    fun shape(): ClickableSurfaceShape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(cornerRadius))

    @Composable
    fun scale(): ClickableSurfaceScale = ClickableSurfaceDefaults.scale(focusedScale = 1f)

    @Composable
    fun colors(
        restingContainer: Color,
        restingContent: Color = Color(TELLY_TEXT_PRIMARY),
    ): ClickableSurfaceColors =
        ClickableSurfaceDefaults.colors(
            containerColor = restingContainer,
            contentColor = restingContent,
            focusedContainerColor = Color(TELLY_FOCUS_FILL),
            focusedContentColor = Color(TELLY_FOCUS_TEXT),
            pressedContainerColor = Color(TELLY_FOCUS_FILL),
            pressedContentColor = Color(TELLY_FOCUS_TEXT),
            disabledContainerColor = restingContainer,
            disabledContentColor = Color(TELLY_TEXT_DISABLED),
        )
}
