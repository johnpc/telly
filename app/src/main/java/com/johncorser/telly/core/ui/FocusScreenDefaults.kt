package com.johncorser.telly.core.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
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
 * "focus me on appear" pattern of menus, cards and key anchors. [yielded]
 * is the container's stand-down signal (see [FocusSeed]): once focus has
 * landed anywhere in the host list, the retry loop must stop — a
 * still-retrying initial grab would otherwise YANK focus back from a row
 * the user (or the test harness) deliberately moved to (the Assign-EPG
 * picker activating "Auto (tvg-id)" instead of the picked id).
 */
@Composable
fun Modifier.focusOnAppear(
    enabled: Boolean = true,
    yielded: () -> Boolean = { false },
): Modifier {
    val grab = stickyFocusGrab(remember { FocusRequester() }, yielded)
    return if (enabled) then(grab) else this
}

/**
 * Container-side stop signal for [focusOnAppear] initial grabs: put
 * [modifier] on the row container and hand [seeded] to the rows. Once ANY
 * descendant holds focus the appear-grabs stand down, so a deliberate
 * focus move during the grab window is never reverted.
 */
class FocusSeed internal constructor(private val state: MutableState<Boolean>) {
    val seeded: () -> Boolean = { state.value }

    fun modifier(): Modifier = Modifier.onFocusChanged { if (it.hasFocus) state.value = true }
}

/** A fresh [FocusSeed] per container composition. */
@Composable
fun rememberFocusSeed(): FocusSeed = FocusSeed(remember { mutableStateOf(false) })

/**
 * The sticky grab as a standalone modifier over a caller-owned
 * [requester]: bounded retry, held back until the node is actually
 * PLACED — requesting focus on an attached-but-unplaced node crashes in
 * the focus system's own bring-into-view coroutine (see
 * [grabFocusUntilLanded]). LazyColumn items that mount as the CURRENT
 * focus target need exactly this gate: a command-scrolled list
 * subcomposes the target item in the middle of the list's measure pass,
 * and a raw LaunchedEffect-requestFocus there fires before anything is
 * placed (the group-tool panel crash). [yielded] stops the retries once
 * the host container reports focus landed elsewhere (see [FocusSeed]).
 */
@Composable
fun stickyFocusGrab(
    requester: FocusRequester,
    yielded: () -> Boolean = { false },
): Modifier {
    val landed = remember { mutableStateOf(false) }
    val placed = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        grabFocusUntilLanded(
            landed = { landed.value || yielded() },
            request = { requester.requestFocus() },
            awaitFrame = { withFrameNanos { } },
            placed = { placed.value },
        )
    }
    return Modifier
        .focusRequester(requester)
        .onPlaced { placed.value = true }
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
