package com.johncorser.telly.features.playback

import com.johncorser.telly.features.epg.NowNext
import com.johncorser.telly.features.player.VideoDetails
import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.testProgram
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.GregorianCalendar
import java.util.TimeZone

class PlaybackInfoBuilderTest {
    private val utc = TimeZone.getTimeZone("UTC")

    private fun at(
        hour: Int,
        minute: Int,
    ): Long =
        GregorianCalendar(utc)
            .apply {
                set(2026, 8, 13, hour, minute, 0)
                set(GregorianCalendar.MILLISECOND, 0)
            }.timeInMillis

    @Test
    fun `builds the overlay lines of capture 34`() {
        val channel = testChannel(1, 1, "News One")
        val nowNext =
            NowNext(
                now = testProgram("tvg-1", at(14, 30), at(15, 45), "Business Hour", episode = "S1 E7"),
                next = testProgram("tvg-1", at(15, 45), at(17, 15), "Newsroom Live", episode = "S1 E8"),
            )

        val data =
            PlaybackInfoBuilder.build(
                channel,
                nowNext,
                VideoDetails(1280, 720, 25f, 1),
                at(14, 45),
                ClockStyle(utc),
            )

        assertEquals(1, data.number)
        assertEquals("News One", data.name)
        assertEquals("News", data.group)
        assertEquals("http://logo/1.png", data.logoUrl)
        assertEquals("Sun, Sep 13, 2:45 PM", data.clockText)
        assertEquals("Business Hour. S1 E7", data.title)
        assertEquals("02:30 — 03:45 PM", data.timeRange)
        assertEquals("60 min", data.remaining)
        assertEquals(200, data.progressPermille)
        assertEquals("03:45 — 05:15 PM  Newsroom Live. S1 E8", data.nextLine)
        assertEquals(listOf("HD", "25 FPS", "MONO"), data.badges)
    }

    @Test
    fun `the description of the airing programme feeds the zap overlay`() {
        val nowNext = NowNext(now = testProgram("tvg-1", at(14, 30), at(15, 45), "Business Hour"))

        val data = PlaybackInfoBuilder.build(testChannel(1, 1, "News One"), nowNext, null, at(14, 45), ClockStyle(utc))

        assertEquals("Description of Business Hour", data.description)
    }

    @Test
    fun `a channel without guide data keeps every line empty`() {
        val data =
            PlaybackInfoBuilder.build(
                testChannel(1, 1, "News One"),
                NowNext(),
                null,
                at(14, 45),
                ClockStyle(utc),
            )

        assertNull(data.title)
        assertNull(data.description)
        assertNull(data.timeRange)
        assertNull(data.remaining)
        assertNull(data.nextLine)
        assertEquals(0, data.progressPermille)
        assertEquals(emptyList<String>(), data.badges)
    }
}
