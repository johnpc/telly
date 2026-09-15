package com.johncorser.telly.features.catchup

import com.johncorser.telly.testutil.FakeProgramDao
import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.testEpgRepository
import com.johncorser.telly.testutil.testProgram
import com.johncorser.telly.testutil.withCatchup
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CatchupNeighboursTest {
    private val hourMs = 3_600_000L
    private val dayMs = 24 * hourMs
    private val now = 100 * dayMs
    private val channel =
        testChannel(1, 1, "News One").withCatchup(source = "http://s/arc?utc={utc}&d={duration}", days = 2)

    private val programs =
        FakeProgramDao(
            listOf(
                testProgram("tvg-1", now - 7 * hourMs, now - 6 * hourMs, "Two Back"),
                testProgram("tvg-1", now - 6 * hourMs, now - 5 * hourMs, "Prev Show"),
                testProgram("tvg-1", now - 5 * hourMs, now - 4 * hourMs, "Current Archive"),
                testProgram("tvg-1", now - 4 * hourMs, now - 3 * hourMs, "Next Show"),
                testProgram("tvg-1", now - hourMs, now + hourMs, "Airing Now"),
            ),
        )

    private val neighbours = CatchupNeighbours(testEpgRepository(programs)) { now }

    private fun requestFor(
        startMs: Long,
        endMs: Long,
    ): CatchupRequest = CatchupRequest(channel, "http://s/arc?utc=$startMs", "X", startMs, endMs)

    @Test
    fun `previous resolves the newest programme ending at the archive's start`() =
        runTest {
            val previous = neighbours.previous(requestFor(now - 5 * hourMs, now - 4 * hourMs))

            assertEquals("Prev Show", previous?.title)
            assertEquals(now - 6 * hourMs, previous?.startMs)
            assertTrue(previous!!.url.contains("utc=${(now - 6 * hourMs) / 1000}"))
        }

    @Test
    fun `previous outside the catchup-days horizon stays put`() =
        runTest {
            programs.programs.value =
                listOf(
                    testProgram("tvg-1", now - 2 * dayMs - hourMs, now - 2 * dayMs, "Too Old"),
                    testProgram("tvg-1", now - 2 * dayMs, now - 2 * dayMs + hourMs, "Edge Archive"),
                )

            assertNull(neighbours.previous(requestFor(now - 2 * dayMs, now - 2 * dayMs + hourMs)))
        }

    @Test
    fun `previous at an EPG edge stays put`() =
        runTest {
            assertNull(neighbours.previous(requestFor(now - 7 * hourMs, now - 6 * hourMs)))
        }

    @Test
    fun `next resolves the following programme's archive`() =
        runTest {
            val jump = neighbours.next(requestFor(now - 5 * hourMs, now - 4 * hourMs))

            val archive = jump as CatchupJump.Archive
            assertEquals("Next Show", archive.request.title)
            assertEquals(now - 4 * hourMs, archive.request.startMs)
        }

    @Test
    fun `next of the programme adjoining the airing one returns to live`() =
        runTest {
            // The neighbour of [now-2h, now-1h] is the airing programme.
            programs.programs.value =
                listOf(
                    testProgram("tvg-1", now - 2 * hourMs, now - hourMs, "Newest Archive"),
                    testProgram("tvg-1", now - hourMs, now + hourMs, "Airing Now"),
                )

            assertEquals(CatchupJump.Live, neighbours.next(requestFor(now - 2 * hourMs, now - hourMs)))
        }

    @Test
    fun `next at an EPG edge returns to live`() =
        runTest {
            programs.programs.value =
                listOf(testProgram("tvg-1", now - 5 * hourMs, now - 4 * hourMs, "Current Archive"))

            assertEquals(CatchupJump.Live, neighbours.next(requestFor(now - 5 * hourMs, now - 4 * hourMs)))
        }

    @Test
    fun `a channel without a tvg id has no neighbours`() =
        runTest {
            val bare = testChannel(2, 2, "No Tvg", tvgId = null).withCatchup()
            val request = CatchupRequest(bare, "u", null, now - 5 * hourMs, now - 4 * hourMs)

            assertNull(neighbours.previous(request))
            assertEquals(CatchupJump.Live, neighbours.next(request))
        }
}
