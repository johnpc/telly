package com.johncorser.telly.features.pip

/**
 * The HOME-press decision behind Settings → General → "Switch to
 * picture-in-picture mode on press Home" (TellySettings.PIP_ON_HOME):
 * onUserLeaveHint enters PIP only when the toggle is on, fullscreen
 * playback is the top route (not the guide, settings sheet, multiview,
 * search, ...), and the activity is not already in the PIP window.
 */
object PipHomePolicy {
    fun shouldEnterOnHome(
        pipOnHome: Boolean,
        playbackFullscreen: Boolean,
        inPip: Boolean,
    ): Boolean = pipOnHome && playbackFullscreen && !inPip
}
