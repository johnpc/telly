package com.johncorser.telly.features.guide

import com.johncorser.telly.features.guide.GuideTestData.at
import com.johncorser.telly.features.guide.GuideTestData.nowMs
import com.johncorser.telly.features.guide.GuideTestData.utc
import com.johncorser.telly.features.playback.ClockStyle
import com.johncorser.telly.testutil.asFavorite
import com.johncorser.telly.testutil.describedAs
import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.testProgram
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GuideInfoBuilderTest {
    private val program =
        testProgram("tvg-1", at(14, 30), at(15, 45), "Business Hour", episode = "S1 E7")
            .describedAs("All-new episode.")
    private val airing = GuideCell(program.startMs, program.endMs, program)
    private val row = GuideRow(testChannel(1, 1, "News One", group = "News"), 1, listOf(airing))

    @Test
    fun `an airing cell shows title, range, remaining, progress and description`() {
        val info = GuideInfoBuilder.build(row, airing, nowMs, ClockStyle(utc))

        assertEquals("Business Hour. S1 E7", info.title)
        assertEquals("02:30 — 03:45 PM", info.range)
        assertEquals("67 min", info.remaining)
        assertEquals(106, info.progressPermille)
        assertEquals("All-new episode.", info.description)
        assertEquals("News", info.group)
        assertEquals(false, info.favorite)
    }

    @Test
    fun `a future cell drops the remaining time and progress`() {
        val next = testProgram("tvg-1", at(15, 45), at(17, 15), "Newsroom Live")
        val cell = GuideCell(next.startMs, next.endMs, next)

        val info = GuideInfoBuilder.build(row, cell, nowMs, ClockStyle(utc))

        assertEquals("Newsroom Live", info.title)
        assertEquals("03:45 — 05:15 PM", info.range)
        assertNull(info.remaining)
        assertNull(info.progressPermille)
    }

    @Test
    fun `a filler cell reads no information`() {
        val filler = GuideCell(at(15, 45), at(16, 15), program = null)

        val info = GuideInfoBuilder.build(row, filler, nowMs, ClockStyle(utc))

        assertEquals(GuideInfoBuilder.NO_INFORMATION, info.title)
        assertNull(info.description)
    }

    @Test
    fun `the favorite star follows the channel flag`() {
        val favoriteRow = row.copy(channel = row.channel.asFavorite())

        assertEquals(true, GuideInfoBuilder.build(favoriteRow, airing, nowMs, ClockStyle(utc)).favorite)
    }
}
