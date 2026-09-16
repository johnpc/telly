package com.johncorser.telly.features.guide

import com.johncorser.telly.features.recording.RecordingPrompt

/**
 * Pure mapping from the recording slice's host-agnostic [RecordingPrompt]s
 * onto guide layers: completed actions land back on the grid (the
 * favorites/hide precedent), and the Stop confirm and the custom form get
 * their own layers.
 */
object GuideRecordingPrompts {
    fun layerFor(
        prompt: RecordingPrompt,
        current: GuideLayer,
    ): GuideLayer =
        when (prompt) {
            RecordingPrompt.Done -> GuideLayer.Grid
            is RecordingPrompt.StopConfirm ->
                GuideLayer.RecordingStop(prompt.recordingId, prompt.channelName)
            RecordingPrompt.CustomForm -> GuideLayer.CustomRecording(back = current)
        }
}
