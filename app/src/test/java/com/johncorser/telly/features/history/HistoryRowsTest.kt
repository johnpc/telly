package com.johncorser.telly.features.history

import com.johncorser.telly.features.history.db.WatchHistoryEntity
import com.johncorser.telly.features.playback.ClockStyle
import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.testProgram
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.TimeZone

class HistoryRowsTest {
    private val utc = TimeZone.getTimeZone("UTC")
    private val channels =
        listOf(
            testChannel(1, 1, "News One"),
            testChannel(2, 2, "Sports Arena"),
            testChannel(4, 4, "Silent FM", tvgId = null),
        )

    // Events arrive newest-first, the DAO's ordering.
    private val events =
        listOf(
            WatchHistoryEntity("tvg-2", 5_000L),
            WatchHistoryEntity("tvg-gone", 4_000L),
            WatchHistoryEntity("tvg-1", 1_500L),
        )

    @Test
    fun `recent channels keep the newest-first order and drop unknown keys`() {
        val recent = HistoryRows.recentChannels(events, channels, currentChannelId = null)

        assertEquals(listOf(2L, 1L), recent.map { it.id })
    }

    @Test
    fun `the playing channel drops out of the recent row`() {
        val recent = HistoryRows.recentChannels(events, channels, currentChannelId = 2L)

        assertEquals(listOf(1L), recent.map { it.id })
    }

    @Test
    fun `rows resolve the programme airing at the watch time`() {
        val programs =
            listOf(
                testProgram("tvg-1", 1_000L, 2_000L, "Business Hour", episode = "S1 E7"),
                testProgram("tvg-1", 2_000L, 3_000L, "Newsroom Live"),
            )

        val rows = HistoryRows.rows(events, channels, programs, ClockStyle(utc))

        assertEquals(listOf(2L, 1L), rows.map { it.channel.id })
        assertNull(rows[0].programmeTitle)
        assertEquals("Business Hour. S1 E7", rows[1].programmeTitle)
        assertEquals("Thu, Jan 1, 12:00 AM", rows[1].watchedText)
    }

    @Test
    fun `a programme ending exactly at the watch time does not match`() {
        val programs = listOf(testProgram("tvg-1", 1_000L, 1_500L, "Business Hour"))

        val rows = HistoryRows.rows(events, channels, programs, ClockStyle(utc))

        assertNull(rows[1].programmeTitle)
    }

    @Test
    fun `the programme span covers the oldest and newest watch`() {
        assertEquals(1_500L..5_001L, HistoryRows.programmeSpan(events))
        assertEquals(0L..1L, HistoryRows.programmeSpan(emptyList()))
    }

    @Test
    fun `the identity key falls back to stream and name without a tvg id`() {
        val silent = listOf(WatchHistoryEntity("http://s/4.ts|Silent FM", 9_000L))

        val rows = HistoryRows.rows(silent, channels, emptyList(), ClockStyle(utc))

        assertEquals(listOf(4L), rows.map { it.channel.id })
    }
}
