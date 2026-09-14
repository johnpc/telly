package com.johncorser.telly.features.guide

import com.johncorser.telly.features.guide.GuideTestData.at
import com.johncorser.telly.testutil.testProgram
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GuideCellsBuilderTest {
    private val span = GuideSpan(at(14, 0), at(17, 0))

    @Test
    fun `contiguous programmes become cells without fillers`() {
        val cells =
            GuideCellsBuilder.build(
                listOf(
                    testProgram("tvg", at(14, 0), at(15, 45), "A"),
                    testProgram("tvg", at(15, 45), at(17, 0), "B"),
                ),
                span,
            )

        assertEquals(listOf("A", "B"), cells.map { it.program?.details?.title })
        assertTrue(cells.all { it.hasInfo })
    }

    @Test
    fun `an EPG-less channel fills the span with 30-minute no-information cells`() {
        val cells = GuideCellsBuilder.build(emptyList(), span)

        assertEquals(6, cells.size)
        assertTrue(cells.none { it.hasInfo })
        assertEquals(at(14, 0), cells.first().startMs)
        assertEquals(at(17, 0), cells.last().endMs)
        assertTrue(cells.zipWithNext().all { (a, b) -> a.endMs == b.startMs })
    }

    @Test
    fun `a mid-schedule gap chunks on the span's half-hour grid`() {
        val cells =
            GuideCellsBuilder.build(
                listOf(
                    testProgram("tvg", at(14, 0), at(14, 45), "A"),
                    testProgram("tvg", at(16, 0), at(17, 0), "B"),
                ),
                span,
            )

        val fillers = cells.filter { !it.hasInfo }
        assertEquals(
            listOf(at(14, 45) to at(15, 0), at(15, 0) to at(15, 30), at(15, 30) to at(16, 0)),
            fillers.map {
                it.startMs to it.endMs
            },
        )
    }

    @Test
    fun `programmes overlapping the span edges keep their true times`() {
        val cells =
            GuideCellsBuilder.build(
                listOf(testProgram("tvg", at(13, 10), at(14, 35), "Early")),
                span,
            )

        assertEquals(at(13, 10), cells.first().startMs)
        assertFalse(cells.last().hasInfo)
        assertEquals(at(17, 0), cells.last().endMs)
    }

    @Test
    fun `programmes fully outside the span are dropped`() {
        val cells =
            GuideCellsBuilder.build(
                listOf(
                    testProgram("tvg", at(12, 0), at(13, 0), "Past"),
                    testProgram("tvg", at(18, 0), at(19, 0), "Future"),
                ),
                span,
            )

        assertTrue(cells.none { it.hasInfo })
    }

    @Test
    fun `a programme swallowed by the previous one is skipped`() {
        val cells =
            GuideCellsBuilder.build(
                listOf(
                    testProgram("tvg", at(14, 0), at(16, 0), "Long"),
                    testProgram("tvg", at(14, 30), at(15, 0), "Swallowed"),
                ),
                span,
            )

        assertEquals(listOf("Long"), cells.mapNotNull { it.program?.details?.title })
    }
}
