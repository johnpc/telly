package com.johncorser.telly.features.recording

import com.johncorser.telly.features.settings.RowIds
import com.johncorser.telly.features.settings.SettingsRow

/** What the Settings -> Other -> Recording pane needs from the DVR slice. */
class RecordingSettingsHook(
    private val center: RecordingCenter,
) {
    fun storage(): RecordingStorageInfo = center.storage()

    suspend fun deleteAll() = center.deleteAll()
}

/** The honest note: telly schedules in-app, not via system alarms. */
const val RECORDING_SCHEDULING_NOTE =
    "Scheduled recordings start only while telly is running"

/**
 * The Recording pane's rows: storage used by captures, the volume's free
 * space, Delete all recordings (GuidedStep confirm) and the scheduling note.
 */
fun recordingRows(hook: RecordingSettingsHook?): List<SettingsRow> {
    val storage = hook?.storage() ?: RecordingStorageInfo(usedBytes = 0, freeBytes = 0)
    return listOf(
        SettingsRow.Value(
            id = RowIds.RECORDING_STORAGE_USED,
            title = "Storage used by recordings",
            summary = RecordingRows.sizeText(storage.usedBytes),
        ),
        SettingsRow.Value(
            id = RowIds.RECORDING_FREE_SPACE,
            title = "Free space",
            summary = RecordingRows.sizeText(storage.freeBytes),
        ),
        SettingsRow.Action(id = RowIds.RECORDING_DELETE_ALL, title = "Delete all recordings"),
        SettingsRow.Note(RECORDING_SCHEDULING_NOTE),
    )
}
