package com.johncorser.telly.features.recording

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.TimeZone

/** What OK / long-OK on a library row asks the user to confirm. */
sealed interface RecordingsConfirm {
    val row: RecordingRow

    /** OK on an in-progress row: stop it (stays in the library as DONE). */
    data class Stop(
        override val row: RecordingRow,
    ) : RecordingsConfirm

    /** OK on a SCHEDULED row: cancel it (row removed). */
    data class Cancel(
        override val row: RecordingRow,
    ) : RecordingsConfirm

    /** Long-OK on any row (and OK on FAILED): delete file + row. */
    data class Delete(
        override val row: RecordingRow,
    ) : RecordingsConfirm
}

/**
 * The Recordings library screen's state: the row list, the GuidedStep
 * confirm for stop/cancel/delete, and which DONE capture is playing
 * fullscreen. A plain class, unit-tested on the JVM.
 */
class RecordingsViewModel(
    private val center: RecordingCenter,
    zone: TimeZone,
    private val scope: CoroutineScope,
) {
    val rows: StateFlow<List<RecordingRow>> =
        center.recordings
            .map { entries -> RecordingRows.rows(entries, zone) }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val mutableConfirm = MutableStateFlow<RecordingsConfirm?>(null)
    val confirm: StateFlow<RecordingsConfirm?> = mutableConfirm.asStateFlow()

    private val mutablePlaying = MutableStateFlow<RecordingRow?>(null)
    val playing: StateFlow<RecordingRow?> = mutablePlaying.asStateFlow()

    /** OK: play DONE captures, confirm stop/cancel/delete for the rest. */
    fun onRowClick(row: RecordingRow) {
        mutableConfirm.value =
            when (row.status) {
                RecordingStatus.DONE -> {
                    mutablePlaying.value = row
                    null
                }
                RecordingStatus.RECORDING -> RecordingsConfirm.Stop(row)
                RecordingStatus.SCHEDULED -> RecordingsConfirm.Cancel(row)
                RecordingStatus.FAILED -> RecordingsConfirm.Delete(row)
            }
    }

    /** Long-OK on any row: delete (file + row) after the confirm. */
    fun onRowLongClick(row: RecordingRow) {
        mutableConfirm.value = RecordingsConfirm.Delete(row)
    }

    /** The confirm's affirmative action. */
    fun onConfirmAccepted() {
        val pending = mutableConfirm.value ?: return
        mutableConfirm.value = null
        scope.launch {
            when (pending) {
                is RecordingsConfirm.Stop -> center.stop(pending.row.entry.id)
                is RecordingsConfirm.Cancel, is RecordingsConfirm.Delete -> center.delete(pending.row.entry.id)
            }
        }
    }

    fun dismissConfirm() {
        mutableConfirm.value = null
    }

    /** BACK from the capture player returns to the list. */
    fun stopPlaying() {
        mutablePlaying.value = null
    }
}
