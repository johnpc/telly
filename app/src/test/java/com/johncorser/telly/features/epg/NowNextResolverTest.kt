package com.johncorser.telly.features.epg

import com.johncorser.telly.features.epg.db.ProgramDetails
import com.johncorser.telly.features.epg.db.ProgramEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NowNextResolverTest {
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
    fun `resolves now and next per channel`() {
        val programs =
            listOf(
                program("a", 0, 100, "A now"),
                program("a", 100, 200, "A next"),
                program("a", 200, 300, "A later"),
                program("b", 40, 90, "B now"),
            )

        val resolved = NowNextResolver.resolve(programs, atMs = 50)

        assertEquals("A now", resolved.getValue("a").now?.details?.title)
        assertEquals("A next", resolved.getValue("a").next?.details?.title)
        assertEquals("B now", resolved.getValue("b").now?.details?.title)
        assertNull(resolved.getValue("b").next)
    }

    @Test
    fun `a gap in the schedule yields no current programme but a next one`() {
        val resolved = NowNextResolver.resolve(listOf(program("a", 100, 200, "Later")), atMs = 50)

        assertNull(resolved.getValue("a").now)
        assertEquals("Later", resolved.getValue("a").next?.details?.title)
    }

    @Test
    fun `boundaries - a programme ending exactly now is not current`() {
        val programs = listOf(program("a", 0, 50, "Ended"), program("a", 50, 100, "Starting"))

        val resolved = NowNextResolver.resolve(programs, atMs = 50)

        assertEquals("Starting", resolved.getValue("a").now?.details?.title)
        assertNull(resolved.getValue("a").next)
    }

    @Test
    fun `empty input resolves to an empty map`() {
        assertTrue(NowNextResolver.resolve(emptyList(), atMs = 0).isEmpty())
    }

    @Test
    fun `now next is a value object`() {
        val nowNext = NowNext(now = program("a", 0, 1, "T"), next = null)
        assertEquals(nowNext, nowNext.copy())
        assertTrue(nowNext.toString().contains("T"))
    }
}
