package com.johncorser.telly.features.mylist

import com.johncorser.telly.features.mylist.db.MyListEntity
import com.johncorser.telly.testutil.testChannel
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone

/** Pure My List row assembly: resolution, auto-hide, airing flag, times. */
class MyListRowsTest {
    private val zone = TimeZone.getTimeZone("UTC")
    private val channels = listOf(testChannel(1, 1, "News One"), testChannel(2, 2, "Sports Arena"))

    private fun entry(
        key: String,
        startMs: Long,
        endMs: Long,
        addedAtMs: Long = startMs,
    ) = MyListEntity(
        channelKey = key,
        startMs = startMs,
        endMs = endMs,
        title = "P$startMs",
        description = null,
        addedAtMs = addedAtMs,
    )

    @Test
    fun `rows keep the store's newest-first order and resolve channels by key`() {
        val entries = listOf(entry("tvg-2", 5_000L, 9_000L), entry("tvg-1", 1_000L, 9_000L))

        val rows = MyListRows.rows(entries, channels, nowMs = 2_000L, zone = zone)

        assertEquals(listOf(2L, 1L), rows.map { it.channel.id })
    }

    @Test
    fun `entries whose channel is gone drop out`() {
        val entries = listOf(entry("vanished", 1_000L, 9_000L))

        assertEquals(emptyList<MyListRow>(), MyListRows.rows(entries, channels, 2_000L, zone))
    }

    @Test
    fun `ended entries auto-hide`() {
        val entries = listOf(entry("tvg-1", 1_000L, 2_000L), entry("tvg-1", 3_000L, 4_000L))

        val rows = MyListRows.rows(entries, channels, nowMs = 2_000L, zone = zone)

        assertEquals(listOf(3_000L), rows.map { it.entry.startMs })
    }

    @Test
    fun `airing follows the entry's span and the air time uses the shared clock format`() {
        val entries = listOf(entry("tvg-1", 1_000L, 9_000L), entry("tvg-2", 5_000L, 9_000L))

        val rows = MyListRows.rows(entries, channels, nowMs = 2_000L, zone = zone)

        assertEquals(listOf(true, false), rows.map { it.airing })
        assertEquals("Thu, Jan 1, 12:00 AM", rows.first().airTimeText)
    }
}
