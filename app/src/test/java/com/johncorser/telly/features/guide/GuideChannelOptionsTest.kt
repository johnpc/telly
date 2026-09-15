package com.johncorser.telly.features.guide

import com.johncorser.telly.features.settings.SettingsRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The "Channel options" pane rows, verbatim from captures 41-42. */
class GuideChannelOptionsTest {
    private val rows = GuideChannelOptions.rows("News One")

    @Test
    fun `every captured row renders locked with its captured value`() {
        val values = rows.filterIsInstance<SettingsRow.Value>()
        assertEquals(
            listOf(
                "Channel name" to "News One",
                "Restore channel name" to "News One",
                "Channel names editor" to "Off",
                "Audio decoder" to "Hardware",
                "Video decoder" to "Hardware",
                "Use external player" to "No",
                "EPG time offset, h:min" to "0:00",
                "Block channel" to null,
                "Hide channel" to null,
            ),
            values.map { it.title to it.summary },
        )
        assertTrue(values.all { it.locked })
        assertEquals(rows.size, values.size)
    }
}
