package com.johncorser.telly.features.recording

import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Per-host adapter between the shared context-sheet rows and the DVR
 * facade: both the guide's sheet and the playback panel's own one of
 * these, feeding [RecordingPrompt]s back through [show] so each host maps
 * them onto its own layer/overlay machine. Also owns the custom-recording
 * form the host renders while [RecordingPrompt.CustomForm] is up.
 */
class RecordingMenu(
    private val center: RecordingCenter,
    private val scope: CoroutineScope,
    private val clock: () -> Long,
    private val show: (RecordingPrompt) -> Unit,
) {
    private val mutableForm = MutableStateFlow<CustomRecordingForm?>(null)

    /** The open custom-recording form (null while none is up). */
    val form: StateFlow<CustomRecordingForm?> = mutableForm.asStateFlow()

    /** The sheet's "Record": instant record, or Stop confirm while live. */
    fun onRecord(channel: ChannelEntity) {
        scope.launch { show(center.toggleInstant(channel)) }
    }

    /** Guide-cell "Record" on a future programme: schedule its slot. */
    fun onRecordProgramme(
        channel: ChannelEntity,
        title: String?,
        startMs: Long,
        endMs: Long,
    ) {
        scope.launch { show(center.scheduleProgramme(channel, title, startMs, endMs)) }
    }

    /** "Custom recording": prime the form and ask the host to show it. */
    fun onCustomRecording(channel: ChannelEntity) {
        if (!RecordingSupport.isRecordable(channel.source.streamUrl)) {
            show(RecordingPrompt.Unsupported(channel.source.name))
            return
        }
        mutableForm.value = CustomRecordingForm(channel, clock())
        show(RecordingPrompt.CustomForm)
    }

    /** The form's Create action: schedule and close. */
    fun createFromForm() {
        val open = mutableForm.value ?: return
        scope.launch {
            center.scheduleProgramme(open.channel, title = null, startMs = open.startMs.value, endMs = open.endMs)
            mutableForm.value = null
            show(RecordingPrompt.Done)
        }
    }

    /** OK on Stop in the GuidedStep confirm. */
    fun confirmStop(recordingId: Long) {
        scope.launch {
            center.stop(recordingId)
            show(RecordingPrompt.Done)
        }
    }

    /** BACK out of the form drops its state. */
    fun dismissForm() {
        mutableForm.value = null
    }
}
