package com.johncorser.telly.features.multiview

import com.johncorser.telly.testutil.testProgram
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone

class MultiviewScheduleTest {
    private val utc = TimeZone.getTimeZone("UTC")
    private val hour = 3_600_000L

    @Test
    fun `rows are chronological with bare start stamps and the airing flag`() {
        val rows =
            MultiviewSchedule.build(
                programs =
                    listOf(
                        testProgram("tvg-1", 5 * hour, 6 * hour, "Later Show"),
                        testProgram("tvg-1", 4 * hour, 5 * hour, "Morning Report", episode = "S1 E20"),
                    ),
                atMs = 4 * hour + 30 * 60_000L,
                zone = utc,
            )

        assertEquals(listOf("04:00 AM", "05:00 AM"), rows.map { it.timeText })
        assertEquals(listOf("Morning Report. S1 E20", "Later Show"), rows.map { it.title })
        assertEquals(listOf(true, false), rows.map { it.airing })
    }

    @Test
    fun `a programme ending exactly now is no longer airing`() {
        val rows = MultiviewSchedule.build(listOf(testProgram("tvg-1", 0, hour, "Done")), atMs = hour, zone = utc)

        assertEquals(false, rows.single().airing)
    }
}
