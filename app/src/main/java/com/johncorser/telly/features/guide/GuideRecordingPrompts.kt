package com.johncorser.telly.features.guide

import com.johncorser.telly.features.playback.PlayerMenuItem
import com.johncorser.telly.features.recording.RecordingPrompt
import com.johncorser.telly.features.recording.RecordingSupport

/**
 * Pure mapping from the recording slice's host-agnostic [RecordingPrompt]s
 * onto guide layers: completed actions land back on the grid (the
 * favorites/hide precedent), the Stop confirm and the custom form get
 * their own layers, and HLS channels reuse the Description explainer.
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
            is RecordingPrompt.Unsupported ->
                GuideLayer.Description(
                    title = PlayerMenuItem.RECORD.label,
                    text = RecordingSupport.HLS_MESSAGE,
                    back = current,
                )
            RecordingPrompt.CustomForm -> GuideLayer.CustomRecording(back = current)
        }
}
