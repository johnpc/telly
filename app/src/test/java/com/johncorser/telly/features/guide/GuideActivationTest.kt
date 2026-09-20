package com.johncorser.telly.features.guide

import com.johncorser.telly.features.guide.GuideTestData.at
import com.johncorser.telly.features.guide.GuideTestData.cell
import com.johncorser.telly.features.guide.GuideTestData.nowMs
import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.withCatchup
import org.junit.Assert.assertEquals
import org.junit.Test

class GuideActivationTest {
    private val airing = cell(at(14, 30), at(15, 45))
    private val future = cell(at(15, 45), at(17, 0))
    private val past = cell(at(13, 0), at(14, 30))
    private val row = GuideRow(testChannel(2, 2, "News One HD"), 2, listOf(past, airing, future))

    @Test
    fun `ok on an airing programme plays that row's channel`() {
        assertEquals(GuideAction.PlayChannel(row.channel), GuideActivation.activate(row, airing, nowMs))
    }

    @Test
    fun `ok on a non-airing programme also plays the channel`() {
        // The dropdown moved to long-OK; a regular OK on any non-catch-up cell
        // jumps straight to the full player (director round).
        assertEquals(GuideAction.PlayChannel(row.channel), GuideActivation.activate(row, future, nowMs))
        assertEquals(GuideAction.PlayChannel(row.channel), GuideActivation.activate(row, past, nowMs))
    }

    @Test
    fun `an airing no-information cell still plays`() {
        val noInfo = GuideCell(at(14, 30), at(15, 0), program = null)

        assertEquals(GuideAction.PlayChannel(row.channel), GuideActivation.activate(row, noInfo, nowMs))
    }

    @Test
    fun `a past cell on a catch-up channel within horizon plays the archive`() {
        val capable = GuideRow(row.channel.withCatchup(days = 7), 2, row.cells)

        assertEquals(GuideAction.PlayCatchup(capable.channel, past), GuideActivation.activate(capable, past, nowMs))
        // A past filler cell (no programme) and airing/future cells just play.
        val filler = GuideCell(at(13, 0), at(13, 30), program = null)
        assertEquals(GuideAction.PlayChannel(capable.channel), GuideActivation.activate(capable, filler, nowMs))
        assertEquals(GuideAction.PlayChannel(capable.channel), GuideActivation.activate(capable, airing, nowMs))
        assertEquals(GuideAction.PlayChannel(capable.channel), GuideActivation.activate(capable, future, nowMs))
    }
}
