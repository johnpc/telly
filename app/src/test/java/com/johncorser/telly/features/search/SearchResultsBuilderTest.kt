package com.johncorser.telly.features.search

import com.johncorser.telly.features.epg.NowNext
import com.johncorser.telly.features.playback.ClockStyle
import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.testProgram
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.TimeZone

class SearchResultsBuilderTest {
    private val style = ClockStyle(TimeZone.getTimeZone("UTC"))

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
    fun `programme matches group per channel and drop unknown tvg ids`() {
        val known = testProgram("one", now, now + hour, "Newsroom Live")
        val unknown = testProgram("ghost", now, now + hour, "Newsroom Live")

        val groups =
            SearchResultsBuilder.programs(
                matches = listOf(known, unknown),
                channels = listOf(newsOne),
                atMs = now,
                style = style,
            )

        assertEquals(1, groups.size)
        assertEquals("News One", groups[0].channel.source.name)
        assertEquals(listOf("Newsroom Live"), groups[0].airings.map { it.title })
    }

    @Test
    fun `channel groups order by name case-insensitively with number ties`() {
        val alpha = testChannel(3, 3, "alpha news", tvgId = "alpha")
        val matches =
            listOf(
                testProgram("two", now, now + hour, "A"),
                testProgram("alpha", now + hour, now + 2 * hour, "A"),
                testProgram("one", now + 2 * hour, now + 3 * hour, "A"),
            )

        val groups =
            SearchResultsBuilder.programs(
                matches = matches,
                channels = listOf(newsOne, newsTwo, alpha),
                atMs = now,
                style = style,
            )

        assertEquals(listOf("alpha news", "News One", "News Two"), groups.map { it.channel.source.name })
    }

    @Test
    fun `airings stay chronological within their channel and repeats are never deduped`() {
        val groups =
            SearchResultsBuilder.programs(
                matches =
                    listOf(
                        testProgram("one", now + 3 * hour, now + 4 * hour, "Newsroom Live"),
                        testProgram("one", now, now + hour, "Newsroom Live"),
                        testProgram("one", now + hour, now + 2 * hour, "Newsroom Live"),
                    ),
                channels = listOf(newsOne),
                atMs = now,
                style = style,
            )

        assertEquals(1, groups.size)
        assertEquals(3, groups[0].airings.size)
        assertEquals(listOf(now, now + hour, now + 3 * hour), groups[0].airings.map { it.program.startMs })
        assertEquals(listOf("Newsroom Live", "Newsroom Live", "Newsroom Live"), groups[0].airings.map { it.title })
    }

    @Test
    fun `same-titled programmes never merge across channels`() {
        val groups =
            SearchResultsBuilder.programs(
                matches =
                    listOf(
                        testProgram("one", now, now + hour, "Newsroom Live"),
                        testProgram("two", now, now + hour, "Newsroom Live"),
                    ),
                channels = listOf(newsOne, newsTwo),
                atMs = now,
                style = style,
            )

        assertEquals(2, groups.size)
        assertEquals(listOf("Newsroom Live"), groups[0].airings.map { it.title })
        assertEquals(listOf("Newsroom Live"), groups[1].airings.map { it.title })
    }

    @Test
    fun `airing programme rows carry dash progress and remaining minutes`() {
        val groups =
            SearchResultsBuilder.programs(
                matches = listOf(testProgram("one", now - hour, now + hour, "Newsroom Live")),
                channels = listOf(newsOne),
                atMs = now,
                style = style,
            )

        assertEquals(500, groups[0].airings[0].progressPermille)
        assertEquals("60 min", groups[0].airings[0].remaining)
    }

    @Test
    fun `upcoming programme rows carry no progress or remaining`() {
        val groups =
            SearchResultsBuilder.programs(
                matches = listOf(testProgram("one", now + hour, now + 2 * hour, "Newsroom Live")),
                channels = listOf(newsOne),
                atMs = now,
                style = style,
            )

        assertEquals(0, groups[0].airings[0].progressPermille)
        assertNull(groups[0].airings[0].remaining)
    }

    @Test
    fun `todays programmes show a bare time range`() {
        val program = testProgram("one", now + hour, now + 2 * hour, "A")

        assertEquals("04:00 — 05:00 PM", SearchResultsBuilder.airTime(program, now, style))
    }

    @Test
    fun `programmes on another day carry the reference date prefix`() {
        val start = now + 10 * hour // Mon Sep 14, 01:00 AM UTC
        val program = testProgram("one", start, start + hour, "A")

        assertEquals("Mon, Sep 14, 01:00 — 02:00 AM", SearchResultsBuilder.airTime(program, now, style))
    }

    @Test
    fun `the range keeps the start meridiem when it differs from the end`() {
        val start = now + 8 * hour // 11:00 PM, still today
        val program = testProgram("one", start, start + 2 * hour, "A")

        assertEquals("11:00 PM — 01:00 AM", SearchResultsBuilder.airTime(program, now, style))
    }

    @Test
    fun `the first visible channel wins a shared tvg id`() {
        val duplicate = testChannel(9, 9, "News One Mirror", tvgId = "one")

        val groups =
            SearchResultsBuilder.programs(
                matches = listOf(testProgram("one", now, now + hour, "A")),
                channels = listOf(newsOne, duplicate),
                atMs = now,
                style = style,
            )

        assertEquals("News One", groups[0].channel.source.name)
    }
}
