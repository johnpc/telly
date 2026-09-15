package com.johncorser.telly.features.recording

import com.johncorser.telly.features.playback.ProgramTimes
import com.johncorser.telly.features.recording.db.RecordingEntity
import java.util.Locale
import java.util.TimeZone

/** One library row: the entry plus its render-ready texts. */
data class RecordingRow(
    val entry: RecordingEntity,
    val status: RecordingStatus,
    /** "Sun, Sep 13, 2:45 PM" — the (planned) start, shared clock format. */
    val dateText: String,
    /** Status detail: "Scheduled" / "Recording" / "42 min · 12.3 MB" / "Failed". */
    val detailText: String,
)

/** Pure assembly + formatting for the Recordings library list. */
object RecordingRows {
    fun rows(
        entries: List<RecordingEntity>,
        zone: TimeZone,
    ): List<RecordingRow> =
        entries.map { entry ->
            val status = entry.recordingStatus
            RecordingRow(
                entry = entry,
                status = status,
                dateText = ProgramTimes.clock(entry.startMs, zone),
                detailText = detailOf(entry, status),
            )
        }

    private fun detailOf(
        entry: RecordingEntity,
        status: RecordingStatus,
    ): String =
        when (status) {
            RecordingStatus.SCHEDULED -> "Scheduled"
            RecordingStatus.RECORDING -> "Recording"
            RecordingStatus.FAILED -> "Failed"
            RecordingStatus.DONE ->
                "${durationText(entry.startMs, entry.endMs ?: entry.plannedEndMs)} · ${sizeText(entry.sizeBytes)}"
        }

    /** Whole minutes, rounded up; captures shorter than a minute say so. */
    fun durationText(
        startMs: Long,
        endMs: Long,
    ): String {
        val minutes = ((endMs - startMs).coerceAtLeast(0) + MINUTE_MS - 1) / MINUTE_MS
        return if (minutes < 1) "under 1 min" else "$minutes min"
    }

    fun sizeText(bytes: Long): String =
        when {
            bytes >= GIGABYTE -> String.format(Locale.US, "%.2f GB", bytes / GIGABYTE.toDouble())
            bytes >= MEGABYTE -> String.format(Locale.US, "%.1f MB", bytes / MEGABYTE.toDouble())
            else -> "${bytes / KILOBYTE} KB"
        }

    private const val MINUTE_MS = 60_000L
    private const val KILOBYTE = 1_024L
    private const val MEGABYTE = KILOBYTE * KILOBYTE
    private const val GIGABYTE = MEGABYTE * KILOBYTE
}
