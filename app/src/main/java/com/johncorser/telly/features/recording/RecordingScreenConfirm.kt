package com.johncorser.telly.features.recording

import androidx.compose.runtime.Composable
import com.johncorser.telly.R
import com.johncorser.telly.features.settings.SettingsScreenGuidedStep

/**
 * "Stop recording?" GuidedStep behind a second Record on a channel that is
 * already being captured (guide sheet + panel sheet hosts).
 */
@Composable
fun RecordingScreenStopConfirm(
    channelName: String,
    onStop: () -> Unit,
    onDismiss: () -> Unit,
) {
    SettingsScreenGuidedStep(
        iconRes = R.drawable.ic_settings_warning,
        title = "Stop recording?",
        bodyLines = listOf("The recording of \"$channelName\" will stop and stay in the Recordings library"),
        actions = listOf("Stop" to onStop, "Cancel" to onDismiss),
    )
}

/** The library's OK / long-OK confirms: stop, cancel-scheduled, delete. */
@Composable
internal fun RecordingScreenLibraryConfirm(
    confirm: RecordingsConfirm,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
) {
    val title = confirm.row.entry.title
    val (headline, body, action) =
        when (confirm) {
            is RecordingsConfirm.Stop ->
                Triple("Stop recording?", "\"$title\" will stop and stay in the library", "Stop")
            is RecordingsConfirm.Cancel ->
                Triple("Cancel scheduled recording?", "\"$title\" will not be recorded", "Remove")
            is RecordingsConfirm.Delete ->
                Triple("Delete recording?", "\"$title\" and its file will be deleted", "Delete")
        }
    SettingsScreenGuidedStep(
        iconRes = R.drawable.ic_settings_warning,
        title = headline,
        bodyLines = listOf(body),
        actions = listOf(action to onAccept, "Cancel" to onDismiss),
    )
}
