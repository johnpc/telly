package com.johncorser.telly.features.pip

import android.app.Activity
import android.app.PictureInPictureParams
import android.os.Build
import android.util.Rational

/**
 * Thin Activity-side glue for picture-in-picture: builds the 16:9 params,
 * fires the platform call, and bridges the activity callbacks into
 * [PipState]/[PipHomePolicy]. Every decision lives in those plain classes;
 * like MainActivity itself this class is entry-point wiring only (the
 * coverage gate's *Activity* exclusion).
 */
class PipActivityBridge(
    private val activity: Activity,
    private val state: PipState,
    private val pipOnHome: () -> Boolean,
    private val playbackFullscreen: () -> Boolean,
) {
    /** Quick-bar slot / HOME: switch to the 16:9 PIP window (API 26+). */
    fun enter() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val ratio = Rational(PIP_ASPECT_WIDTH, PIP_ASPECT_HEIGHT)
        val params = PictureInPictureParams.Builder().setAspectRatio(ratio).build()
        // Devices without the PIP system feature throw IllegalStateException.
        runCatching { activity.enterPictureInPictureMode(params) }
    }

    /** HOME press (onUserLeaveHint): enter PIP per [PipHomePolicy]. */
    fun onUserLeaveHint() {
        if (PipHomePolicy.shouldEnterOnHome(pipOnHome(), playbackFullscreen(), state.inPip.value)) enter()
    }

    /** onPictureInPictureModeChanged: the compose tree reads [PipState]. */
    fun onModeChanged(inPip: Boolean) = state.setInPip(inPip)

    private companion object {
        const val PIP_ASPECT_WIDTH = 16
        const val PIP_ASPECT_HEIGHT = 9
    }
}
