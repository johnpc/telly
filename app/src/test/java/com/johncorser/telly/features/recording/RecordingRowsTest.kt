package com.johncorser.telly.features.recording

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone

class RecordingRowsTest {
    private val zone = TimeZone.getTimeZone("UTC")

    @Test
    fun `rows carry the shared clock stamp and per-status detail`() {
        val entries =
            listOf(
                recordingEntity(status = RecordingStatus.SCHEDULED, startMs = 0L).copy(id = 1),
                recordingEntity(status = RecordingStatus.RECORDING).copy(id = 2),
                recordingEntity(status = RecordingStatus.DONE, startMs = 0L)
                    .copy(id = 3, endMs = 42 * 60_000L, sizeBytes = 12 * 1_024 * 1_024L),
                recordingEntity(status = RecordingStatus.FAILED).copy(id = 4),
            )

        val rows = RecordingRows.rows(entries, zone)

        assertEquals("Thu, Jan 1, 12:00 AM", rows[0].dateText)
        assertEquals(listOf("Scheduled", "Recording", "42 min · 12.0 MB", "Failed"), rows.map { it.detailText })
        assertEquals(RecordingStatus.RECORDING, rows[1].status)
    }

    @Test
    fun `a DONE row without an actual end falls back to the planned end`() {
        val entry =
            recordingEntity(
                status = RecordingStatus.DONE,
                startMs = 0L,
                plannedEndMs = 30 * 60_000L,
            )

        assertEquals("30 min · 0 KB", RecordingRows.rows(listOf(entry), zone).single().detailText)
    }

    @Test
    fun `durations round up to whole minutes`() {
        assertEquals("under 1 min", RecordingRows.durationText(0, 0))
        assertEquals("1 min", RecordingRows.durationText(0, 30_000))
        assertEquals("2 min", RecordingRows.durationText(0, 61_000))
    }

    @Test
    fun `sizes render as KB, MB or GB`() {
        assertEquals("0 KB", RecordingRows.sizeText(0))
        assertEquals("512 KB", RecordingRows.sizeText(512 * 1_024L))
        assertEquals("12.3 MB", RecordingRows.sizeText((12.3 * 1_024 * 1_024).toLong()))
        assertEquals("2.50 GB", RecordingRows.sizeText((2.5 * 1_024 * 1_024 * 1_024).toLong()))
    }
}
