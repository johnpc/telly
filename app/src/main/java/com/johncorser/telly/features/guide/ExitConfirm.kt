package com.johncorser.telly.features.guide

import com.johncorser.telly.core.state.StateMachine
import com.johncorser.telly.features.playback.PlaybackEnv
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
 * toast-length window are used. Injected scope; no wall-clock reads.
 */
class GuideChrome(
    env: PlaybackEnv,
    scope: CoroutineScope,
) {
    /** "Confirm exit by second press Back" state at the guide root. */
    val exit = ExitConfirm(scope)

    /** PIN prompts follow the persisted input method (wheels or keyboard). */
    val keyboardPin: Boolean get() = parental?.usesKeyboardPin == true
    private val parental = env.hooks.parental
}

/**
 * Two states, one field: Idle (no toast) and Armed (toast showing). The
 * toast IS the arming — a BACK exits exactly while the warning is visible,
 * so the window the user sees and the window the policy honors can never
 * drift apart (they used to be timed by two different clocks).
 */
@StateMachine("Exit confirm")
class ExitConfirm(
    private val scope: CoroutineScope,
) {
    private val mutableWarning = MutableStateFlow(false)
    private var hideJob: Job? = null

    /** True while the warning toast should render (= the exit window is armed). */
    val warning: StateFlow<Boolean> = mutableWarning.asStateFlow()

    /** True = exit now (second BACK inside the window); false = warn + arm. */
    fun onBack(): Boolean {
        if (mutableWarning.value) return true
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
