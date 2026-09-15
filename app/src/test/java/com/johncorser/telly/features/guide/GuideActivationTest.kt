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
    fun `ok on an airing programme of another channel tunes the preview`() {
        val action = GuideActivation.activate(row, airing, nowMs, previewChannelId = 1L)

        assertEquals(GuideAction.TunePreview(row.channel), action)
    }

    @Test
    fun `ok with nothing previewed also tunes the preview first`() {
        assertEquals(GuideAction.TunePreview(row.channel), GuideActivation.activate(row, airing, nowMs, null))
    }

    @Test
    fun `a second ok on the previewed channel goes fullscreen`() {
        assertEquals(GuideAction.GoFullscreen, GuideActivation.activate(row, airing, nowMs, previewChannelId = 2L))
    }

    @Test
    fun `ok on a non-airing programme opens the premium dropdown`() {
        assertEquals(GuideAction.OpenCellMenu(future), GuideActivation.activate(row, future, nowMs, 2L))
        assertEquals(GuideAction.OpenCellMenu(past), GuideActivation.activate(row, past, nowMs, 2L))
    }

    @Test
    fun `an airing no-information cell still tunes`() {
        val noInfo = GuideCell(at(14, 30), at(15, 0), program = null)

        assertEquals(GuideAction.TunePreview(row.channel), GuideActivation.activate(row, noInfo, nowMs, 1L))
    }

    @Test
    fun `a past cell on a catch-up channel within horizon plays the archive`() {
        val capable = GuideRow(row.channel.withCatchup(days = 7), 2, row.cells)

        val action = GuideActivation.activate(capable, past, nowMs, 2L)

        assertEquals(GuideAction.PlayCatchup(capable.channel, past), action)
        // A past filler cell and airing/future programmes keep today's map.
        val filler = GuideCell(at(13, 0), at(13, 30), program = null)
        assertEquals(GuideAction.OpenCellMenu(filler), GuideActivation.activate(capable, filler, nowMs, 2L))
        assertEquals(GuideAction.GoFullscreen, GuideActivation.activate(capable, airing, nowMs, 2L))
        assertEquals(GuideAction.OpenCellMenu(future), GuideActivation.activate(capable, future, nowMs, 2L))
    }
}
