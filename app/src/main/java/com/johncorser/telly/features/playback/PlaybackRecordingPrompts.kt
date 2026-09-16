package com.johncorser.telly.features.playback

import com.johncorser.telly.features.recording.RecordingPrompt

/**
 * Pure mapping from the recording slice's host-agnostic [RecordingPrompt]s
 * onto playback overlays: completed actions dismiss the sheet like the
 * favorites/hide rows do, and the Stop confirm and the custom form ride
 * the Pushed-overlay BACK chain.
 */
object PlaybackRecordingPrompts {
    fun overlayFor(
        prompt: RecordingPrompt,
        current: PlaybackOverlay,
    ): PlaybackOverlay =
        when (prompt) {
            RecordingPrompt.Done -> doneTarget(current)
            is RecordingPrompt.StopConfirm ->
                PlaybackOverlay.RecordingStop(prompt.recordingId, prompt.channelName, back = doneTarget(current))
            RecordingPrompt.CustomForm -> PlaybackOverlay.CustomRecording(back = current)
        }

    /** Where a completed DVR action lands: the panel under a sheet, else bare playback. */
    private tailrec fun doneTarget(overlay: PlaybackOverlay): PlaybackOverlay =
        when (overlay) {
            is PlaybackOverlay.ChannelMenu, PlaybackOverlay.Panel -> PlaybackOverlay.Panel
            is PlaybackOverlay.Pushed -> doneTarget(overlay.back)
            else -> PlaybackOverlay.None
        }
}
