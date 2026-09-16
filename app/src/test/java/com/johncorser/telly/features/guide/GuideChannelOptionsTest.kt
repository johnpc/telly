package com.johncorser.telly.features.guide

import com.johncorser.telly.features.playlist.db.ChannelFlags
import com.johncorser.telly.features.playlist.db.ChannelOverrides
import com.johncorser.telly.features.settings.SettingsRow
import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.withOverrides
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The "Channel options" pane rows (captures 41-42), live per-channel state. */
class GuideChannelOptionsTest {
    private val channel = testChannel(1, 1, "News One")

    @Test
    fun `default state renders the captured row set, nothing locked`() {
        val rows = GuideChannelOptions.rows(channel, externalDefault = false)
        assertEquals(
            listOf(
                "Channel name" to "News One",
                "Channel names editor" to null,
                "Audio decoder" to "Default",
                "Video decoder" to "Default",
                "EPG time offset, h:min" to "0:00",
            ),
            rows.filterIsInstance<SettingsRow.Value>().map { it.title to it.summary },
        )
        assertEquals(
            listOf("Use external player" to false),
            rows.filterIsInstance<SettingsRow.Toggle>().map { it.title to it.checked },
        )
        assertEquals(
            listOf("Block channel", "Hide channel"),
            rows.filterIsInstance<SettingsRow.Action>().map { it.title },
        )
        assertTrue(
            rows.none {
                (it as? SettingsRow.Value)?.locked == true ||
                    (it as? SettingsRow.Toggle)?.locked == true ||
                    (it as? SettingsRow.Action)?.locked == true
            },
        )
    }

    @Test
    fun `a custom name shows on the name row and reveals the restore row`() {
        val renamed = channel.withOverrides(ChannelOverrides(customName = "News Uno"))
        val rows = GuideChannelOptions.rows(renamed, externalDefault = false)
        val values = rows.filterIsInstance<SettingsRow.Value>()
        assertEquals("News Uno", values.first { it.title == "Channel name" }.summary)
        // Restore shows the ORIGINAL playlist name it would bring back.
        assertEquals("News One", values.first { it.title == "Restore channel name" }.summary)
    }

    @Test
    fun `decoder and offset overrides render their picked values`() {
        val overridden =
            channel.withOverrides(
                ChannelOverrides(audioDecoder = "Software", videoDecoder = "Hardware", epgOffsetMinutes = -90),
            )
        val values = GuideChannelOptions.rows(overridden, externalDefault = false).filterIsInstance<SettingsRow.Value>()
        assertEquals("Software", values.first { it.title == "Audio decoder" }.summary)
        assertEquals("Hardware", values.first { it.title == "Video decoder" }.summary)
        assertEquals("-1:30", values.first { it.title == "EPG time offset, h:min" }.summary)
    }

    @Test
    fun `the external toggle follows the global until a per-channel override wins`() {
        fun checked(
            raw: String?,
            global: Boolean,
        ): Boolean =
            GuideChannelOptions
                .rows(channel.withOverrides(ChannelOverrides(externalPlayer = raw)), global)
                .filterIsInstance<SettingsRow.Toggle>()
                .single()
                .checked
        assertTrue(checked(raw = null, global = true))
        assertFalse(checked(raw = null, global = false))
        assertFalse(checked(raw = "Off", global = true))
        assertTrue(checked(raw = "On", global = false))
    }

    @Test
    fun `a blocked channel's block row reads unblock like the sheet's`() {
        val blocked = channel.copy(flags = ChannelFlags(blocked = true))
        val actions = GuideChannelOptions.rows(blocked, externalDefault = false).filterIsInstance<SettingsRow.Action>()
        assertEquals(listOf("Unblock channel", "Hide channel"), actions.map { it.title })
    }
}
