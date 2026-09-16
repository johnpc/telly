package com.johncorser.telly.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onPlaced

/** A remembered [FocusScreenReclaim] with its grab engine already mounted. */
@Composable
fun rememberFocusScreenReclaim(): FocusScreenReclaim {
    val reclaim = remember { FocusScreenReclaim() }
    FocusScreenReclaimEffect(reclaim)
    return reclaim
}

/**
 * Mounts the grab engine for a caller-owned [FocusScreenReclaim]: every
 * [FocusScreenReclaim.reclaim] re-runs the placement-gated bounded grab onto the
 * [FocusScreenReclaim.target]-tagged node. Hosted where the reclaim state lives
 * (not on the target node), so a target that remounts never re-triggers a
 * stale grab.
 */
@Composable
fun FocusScreenReclaimEffect(reclaim: FocusScreenReclaim) {
    val epoch = reclaim.epoch.value
    LaunchedEffect(reclaim, epoch) {
        reclaim.grab(epoch, awaitFrame = { withFrameNanos { } })
    }
}

/**
 * A deliberate focus re-land: when an action unmounts the focused node
 * (removing the focused row, clearing the recents tail), the host calls
 * [reclaim] and focus re-lands on the node tagged by [target] — through
 * the same placement-gated bounded retry the appear grabs use
 * ([grabFocusUntilLanded]), because the landing target may have mounted
 * this very frame and a raw LaunchedEffect+requestFocus would fire before
 * it is placed (the uncatchable bring-into-view crash). Unlike an appear
 * grab a reclaim never yields: it IS the user's deliberate move.
 */
class FocusScreenReclaim {
    private val requester = FocusRequester()
    private val placed = mutableStateOf(false)
    private val landed = mutableStateOf(false)
    internal val epoch = mutableStateOf(0)

    /** Schedules a re-grab; the mounted [grab] engine picks it up. */
    fun reclaim() {
        epoch.value++
    }

    internal fun nodePlaced(isPlaced: Boolean) {
        placed.value = isPlaced
    }

    internal fun nodeFocused(hasFocus: Boolean) {
        if (hasFocus) landed.value = true
    }

    /**
     * Tags the node a [reclaim] re-lands focus on: the requester plus the
     * placement / landed signals the gated grab needs. Tag one node at a
     * time; the placement gate re-arms when the tagged node leaves
     * composition (a fresh first row must be placed before it is grabbed).
     */
    @Composable
    fun target(): Modifier {
        DisposableEffect(this) { onDispose { nodePlaced(false) } }
        return Modifier
            .focusRequester(requester)
            .onPlaced { nodePlaced(true) }
            .onFocusChanged { nodeFocused(it.hasFocus) }
    }

    /** One reclaim pass; epoch 0 (nothing reclaimed yet) is a no-op. */
    suspend fun grab(
        epoch: Int,
        awaitFrame: suspend () -> Unit,
    ) {
        if (epoch == 0) return
        landed.value = false
        grabFocusUntilLanded(
            landed = { landed.value },
            request = { requester.requestFocus() },
            awaitFrame = awaitFrame,
            placed = { placed.value },
        )
    }
}
