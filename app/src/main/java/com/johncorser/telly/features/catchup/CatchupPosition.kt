package com.johncorser.telly.features.catchup

import com.johncorser.telly.features.player.PlayerEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The transport's position readout. Sampling is DRIVEN by the overlay (a
 * per-second UI ticker while it is visible) and by explicit seek updates,
 * so the logic layer never spins its own clock loop.
 */
class CatchupPosition(
    private val engine: PlayerEngine,
) {
    private val mutable = MutableStateFlow(0L)

    val position: StateFlow<Long> = mutable.asStateFlow()

    fun reset() {
        mutable.value = 0L
    }

    fun refresh() {
        mutable.value = engine.positionMs()
    }

    fun set(positionMs: Long) {
        mutable.value = positionMs
    }
}
