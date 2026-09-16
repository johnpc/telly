package com.johncorser.telly.features.guide

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * "Confirm exit by second press Back" policy at the guide root: the first
 * BACK arms a window and shows the standard warning toast, a second BACK
 * inside the window exits. The reference dialog/wording is premium-locked
 * and uncapturable, so the stock "Press BACK again to exit" phrasing and a
 * toast-length window are used. Injected clock/scope; no wall-clock reads.
 */
class ExitConfirm(
    private val clock: () -> Long,
    private val scope: CoroutineScope,
) {
    private val mutableWarning = MutableStateFlow(false)
    private var armedUntilMs = Long.MIN_VALUE
    private var hideJob: Job? = null

    /** True while the warning toast should render. */
    val warning: StateFlow<Boolean> = mutableWarning.asStateFlow()

    /** True = exit now (second BACK inside the window); false = warn + arm. */
    fun onBack(): Boolean {
        if (clock() < armedUntilMs) return true
        armedUntilMs = clock() + WINDOW_MS
        mutableWarning.value = true
        hideJob?.cancel()
        hideJob =
            scope.launch {
                delay(WINDOW_MS)
                mutableWarning.value = false
            }
        return false
    }

    companion object {
        const val WINDOW_MS = 5_000L
        const val MESSAGE = "Press BACK again to exit"
    }
}
