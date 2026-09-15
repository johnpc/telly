package com.johncorser.telly.features.recording

import com.johncorser.telly.features.settings.SettingsRow

/** What the Settings -> Other -> Recording pane needs from the DVR slice. */
class RecordingSettingsHook(
    private val center: RecordingCenter,
) {
    fun storage(): RecordingStorageInfo = center.storage()

    suspend fun deleteAll() = center.deleteAll()
}

/** Row ids of the Recording pane (owned by the recording slice). */
object RecordingRowIds {
    const val STORAGE_USED = "recording.storage_used"
    const val FREE_SPACE = "recording.free_space"
    const val DELETE_ALL = "recording.delete_all"
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
            id = RecordingRowIds.STORAGE_USED,
            title = "Storage used by recordings",
            summary = RecordingRows.sizeText(storage.usedBytes),
        ),
        SettingsRow.Value(
            id = RecordingRowIds.FREE_SPACE,
            title = "Free space",
            summary = RecordingRows.sizeText(storage.freeBytes),
        ),
        SettingsRow.Action(id = RecordingRowIds.DELETE_ALL, title = "Delete all recordings"),
        SettingsRow.Note(RECORDING_SCHEDULING_NOTE),
    )
}
