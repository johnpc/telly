package com.johncorser.telly.features.search

import com.johncorser.telly.features.epg.NowNext
import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.testProgram
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.TimeZone

class SearchResultsBuilderTest {
    private val zone = TimeZone.getTimeZone("UTC")

    // Sun Sep 13 2026 15:00:00 UTC
    private val now = 1_789_311_600_000L
    private val hour = 3_600_000L

    private val newsOne = testChannel(1, 1, "News One", tvgId = "one")
    private val newsTwo = testChannel(2, 2, "News Two", tvgId = "two")

    @Test
    fun `channel hits carry the airing programme title and its progress`() {
        val airing = testProgram("one", now - hour, now + hour, "Business Hour", episode = "S1 E7")

        val hits =
            SearchResultsBuilder.channels(
                matches = listOf(newsOne, newsTwo),
                guide = mapOf("one" to NowNext(now = airing)),
                atMs = now,
            )

        assertEquals("Business Hour. S1 E7", hits[0].nowTitle)
        assertEquals(500, hits[0].progressPermille)
        assertNull(hits[1].nowTitle)
        assertEquals(0, hits[1].progressPermille)
    }

    @Test
    fun `programme hits resolve their channel and drop unknown tvg ids`() {
        val known = testProgram("one", now, now + hour, "Newsroom Live")
        val unknown = testProgram("ghost", now, now + hour, "Newsroom Live")

        val hits =
            SearchResultsBuilder.programs(
                matches = listOf(known, unknown),
                channels = listOf(newsOne),
                atMs = now,
                zone = zone,
            )

        assertEquals(1, hits.size)
        assertEquals("News One", hits[0].channel.source.name)
        assertEquals("Newsroom Live", hits[0].title)
    }

    @Test
    fun `only the first row of a same-channel run shows the channel card`() {
        val hits =
            SearchResultsBuilder.programs(
                matches =
                    listOf(
                        testProgram("one", now, now + hour, "A"),
                        testProgram("one", now + hour, now + 2 * hour, "B"),
                        testProgram("two", now + 2 * hour, now + 3 * hour, "C"),
                        testProgram("one", now + 3 * hour, now + 4 * hour, "D"),
                    ),
                channels = listOf(newsOne, newsTwo),
                atMs = now,
                zone = zone,
            )

        assertEquals(listOf(true, false, true, true), hits.map { it.showsChannelCard })
    }

    @Test
    fun `todays programmes show a bare time range`() {
        val program = testProgram("one", now + hour, now + 2 * hour, "A")

        assertEquals("04:00 — 05:00 PM", SearchResultsBuilder.airTime(program, now, zone))
    }

    @Test
    fun `programmes on another day carry the reference date prefix`() {
        val start = now + 10 * hour // Mon Sep 14, 01:00 AM UTC
        val program = testProgram("one", start, start + hour, "A")

        assertEquals("Mon, Sep 14, 01:00 — 02:00 AM", SearchResultsBuilder.airTime(program, now, zone))
    }

    @Test
    fun `the range keeps the start meridiem when it differs from the end`() {
        val start = now + 8 * hour // 11:00 PM, still today
        val program = testProgram("one", start, start + 2 * hour, "A")

        assertEquals("11:00 PM — 01:00 AM", SearchResultsBuilder.airTime(program, now, zone))
    }

    @Test
    fun `the first visible channel wins a shared tvg id`() {
        val duplicate = testChannel(9, 9, "News One Mirror", tvgId = "one")

        val hits =
            SearchResultsBuilder.programs(
                matches = listOf(testProgram("one", now, now + hour, "A")),
                channels = listOf(newsOne, duplicate),
                atMs = now,
                zone = zone,
            )

        assertEquals("News One", hits[0].channel.source.name)
    }
}
