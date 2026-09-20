package com.johncorser.telly.features.epg.db

import com.johncorser.telly.core.db.inMemoryDb
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ProgramDaoTest {
    private val database = inMemoryDb()
    private val dao = database.programDao()

    @After
    fun tearDown() {
        database.close()
    }

    private fun program(
        tvgId: String,
        startMs: Long,
        endMs: Long,
        title: String,
    ) = ProgramEntity(
        channelTvgId = tvgId,
        startMs = startMs,
        endMs = endMs,
        details = ProgramDetails(title = title),
    )

    @Test
    fun `the window query returns overlapping programmes ordered by channel then start`() =
        runTest {
            dao.upsertAll(
                listOf(
                    program("b", 100, 200, "B in"),
                    program("a", 0, 100, "A before"),
                    program("a", 100, 200, "A in"),
                    program("a", 200, 300, "A after"),
                    program("c", 100, 200, "C other channel"),
                ),
            )

            val window = dao.observeWindow(listOf("a", "b"), fromMs = 100, toMs = 200).first()

            assertEquals(listOf("A in", "B in"), window.map { it.details.title })
        }

    @Test
    fun `a programme spanning the window edge is included`() =
        runTest {
            dao.upsertAll(listOf(program("a", 0, 1_000, "Spanning")))

            val window = dao.observeWindow(listOf("a"), fromMs = 400, toMs = 500).first()

            assertEquals(listOf("Spanning"), window.map { it.details.title })
        }

    @Test
    fun `upsert replaces the row with the same channel and start`() =
        runTest {
            dao.upsertAll(listOf(program("a", 100, 200, "Old title")))
            dao.upsertAll(listOf(program("a", 100, 200, "New title")))

            assertEquals(1, dao.count())
            val stored = dao.observeWindow(listOf("a"), 0, 300).first().single()
            assertEquals("New title", stored.details.title)
        }

    @Test
    fun `delete-for removes only the given channels`() =
        runTest {
            dao.upsertAll(listOf(program("a", 0, 1, "A"), program("b", 0, 1, "B")))

            dao.deleteFor(listOf("a"))

            assertEquals(listOf("b"), dao.observeWindow(listOf("a", "b"), 0, 2).first().map { it.channelTvgId })
        }

    @Test
    fun `replace-for swaps a channel's schedule and leaves other channels alone`() =
        runTest {
            dao.upsertAll(listOf(program("a", 0, 1, "A old"), program("b", 0, 1, "B kept")))

            dao.replaceFor(listOf("a"), listOf(program("a", 1, 2, "A new")))

            val rows = dao.observeWindow(listOf("a", "b"), 0, 3).first()
            assertEquals(listOf("A new", "B kept"), rows.map { it.details.title })
        }

    @Test
    fun `pruning drops programmes that ended before the cutoff`() =
        runTest {
            dao.upsertAll(listOf(program("a", 0, 50, "Ended"), program("a", 50, 150, "Still on")))

            dao.deleteEndedBefore(100)

            assertEquals(listOf("Still on"), dao.observeWindow(listOf("a"), 0, 200).first().map { it.details.title })
        }

    @Test
    fun `airing-or-upcoming excludes programmes already over`() =
        runTest {
            dao.upsertAll(
                listOf(
                    program("a", 0, 100, "Over"),
                    program("a", 100, 200, "Now"),
                    program("a", 200, 300, "Next"),
                ),
            )

            val rows = dao.observeAiringOrUpcoming(listOf("a"), atMs = 150).first()

            assertEquals(listOf("Now", "Next"), rows.map { it.details.title })
        }
}
