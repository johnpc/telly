package com.johncorser.telly.features.search

import com.johncorser.telly.testutil.FakeChannelDao
import com.johncorser.telly.testutil.FakeProgramDao
import com.johncorser.telly.testutil.FakeSearchDao
import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.testEpgRepository
import com.johncorser.telly.testutil.testProgram
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class SearchRepositoryTest {
    private val zone = TimeZone.getTimeZone("UTC")
    private val now = 1_789_311_600_000L
    private val hour = 3_600_000L

    private val channels =
        listOf(
            testChannel(1, 1, "News One", tvgId = "one"),
            testChannel(2, 2, "News One HD", tvgId = "hd"),
            testChannel(3, 3, "Sports Arena", tvgId = "sports"),
            testChannel(24, 24, "Music Box", tvgId = "music"),
        )
    private val channelDao = FakeChannelDao(channels)
    private val programDao =
        FakeProgramDao(
            listOf(
                testProgram("one", now - hour, now + hour, "Newsroom Live", episode = "S1 E8"),
                testProgram("one", now + hour, now + 2 * hour, "Weather Watch"),
                testProgram("sports", now - 3 * hour, now - hour, "Newsroom Rerun"),
                testProgram("sports", now + 2 * hour, now + 3 * hour, "World News Now"),
            ),
        )
    private val repository =
        SearchRepository(FakeSearchDao(channelDao, programDao), channelDao, testEpgRepository(programDao))

    @Test
    fun `a blank query returns empty shelves`() =
        runTest {
            assertTrue(repository.search("   ", now, zone).isEmpty)
        }

    @Test
    fun `channels match by name substring in name order with airing programme`() =
        runTest {
            val results = repository.search("news", now, zone)

            assertEquals(listOf("News One", "News One HD"), results.channels.map { it.channel.source.name })
            assertEquals("Newsroom Live. S1 E8", results.channels[0].nowTitle)
            assertEquals(500, results.channels[0].progressPermille)
        }

    @Test
    fun `a digits-only query also matches channel numbers by prefix`() =
        runTest {
            val results = repository.search("2", now, zone)

            // Name order like every channel shelf (tm-02): Music Box (24) < News One HD (2).
            assertEquals(listOf(24, 2), results.channels.map { it.channel.number })
        }

    @Test
    fun `programmes match by title substring per channel and skip ended ones`() =
        runTest {
            val results = repository.search("news", now, zone)

            assertEquals(listOf("News One", "Sports Arena"), results.programs.map { it.channel.source.name })
            assertEquals(listOf("Newsroom Live. S1 E8"), results.programs[0].airings.map { it.title })
            assertEquals(listOf("World News Now"), results.programs[1].airings.map { it.title })
        }

    @Test
    fun `hidden channels are excluded from both shelves`() =
        runTest {
            val hidden = channels[0].copy(flags = channels[0].flags.copy(hidden = true))
            channelDao.update(hidden)

            val results = repository.search("newsroom", now, zone)

            assertTrue(results.channels.isEmpty())
            assertTrue(results.programs.isEmpty())
        }
}
