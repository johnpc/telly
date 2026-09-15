package com.johncorser.telly.features.recording

/**
 * What a Record / Custom recording action wants the hosting surface (guide
 * sheet, panel sheet, cell dropdown) to show next. Hosts map these onto
 * their own layer/overlay types; the recording slice stays host-agnostic.
 */
sealed interface RecordingPrompt {
    /** The action completed (capture started/scheduled/stopped): close chrome. */
    data object Done : RecordingPrompt

    /** A second Record on a recording channel offers Stop (GuidedStep). */
    data class StopConfirm(
        val recordingId: Long,
        val channelName: String,
    ) : RecordingPrompt

    /** HLS channel: show [RecordingSupport.HLS_MESSAGE] instead of recording. */
    data class Unsupported(
        val channelName: String,
    ) : RecordingPrompt

    /** Open the custom-recording form (already primed in [RecordingMenu]). */
    data object CustomForm : RecordingPrompt
}
