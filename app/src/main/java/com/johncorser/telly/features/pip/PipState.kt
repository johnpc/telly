package com.johncorser.telly.features.pip

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Observable picture-in-picture mode. The activity (which owns the real
 * window transitions) writes it from onPictureInPictureModeChanged; the
 * playback slice reads it to hide its chrome and to keep the stream alive
 * while the window is tiny. Always injected; [shared] is the one production
 * instance both sides agree on (the app has no DI container — cf.
 * ServiceLocator).
 */
class PipState {
    private val mutableInPip = MutableStateFlow(false)

    /** True between the enter and exit PIP-mode-changed callbacks. */
    val inPip: StateFlow<Boolean> = mutableInPip.asStateFlow()

    fun setInPip(value: Boolean) {
        mutableInPip.value = value
    }

    /**
     * Activity STOP must stop the stream when the app is really backgrounded
     * (the reference releases its codecs the instant it leaves the screen),
     * but never while the PIP window is up — there the video keeps playing.
     * Closing the PIP window flips the mode off BEFORE the stop lands, so
     * that stop passes and playback ends like any other backgrounding.
     */
    fun allowsBackgroundStop(): Boolean = !inPip.value

    companion object {
        /** The single production instance MainActivity and Compose share. */
        val shared = PipState()
    }
}
