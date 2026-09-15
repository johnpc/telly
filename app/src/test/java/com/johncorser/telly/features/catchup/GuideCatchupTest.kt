package com.johncorser.telly.features.catchup

import com.johncorser.telly.features.epg.db.ProgramDetails
import com.johncorser.telly.features.epg.db.ProgramEntity
import com.johncorser.telly.features.guide.GuideCell
import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.withCatchup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GuideCatchupTest {
    private val hourMs = 3_600_000L
    private val now = 200 * hourMs
    private val session = CatchupSession()
    private var fullscreens = 0
    private val launcher = GuideCatchup(session, { now }) { fullscreens += 1 }

    private fun cell(withProgramme: Boolean = true): GuideCell {
        val start = now - 5 * hourMs
        val end = now - 4 * hourMs
        val program =
            ProgramEntity(
                channelTvgId = "tvg-1",
                startMs = start,
                endMs = end,
                details = ProgramDetails(title = "Morning Report", subTitle = "Pilot"),
            )
        return GuideCell(start, end, program.takeIf { withProgramme })
    }

    @Test
    fun `play resolves the cell to a request and goes fullscreen`() {
        val channel = testChannel(1, 1, "News One").withCatchup(source = "http://s/arc?utc={utc}&d={duration}")

        launcher.play(channel, cell())

        val request = session.consume()
        assertEquals("http://s/arc?utc=${(now - 5 * hourMs) / 1_000}&d=3600", request?.url)
        assertEquals("Morning Report: Pilot", request?.title)
        assertEquals(now - 5 * hourMs, request?.startMs)
        assertEquals(now - 4 * hourMs, request?.endMs)
        assertEquals(1, fullscreens)
    }

    @Test
    fun `a filler cell still plays as a bare time range without a title`() {
        val channel = testChannel(1, 1, "News One").withCatchup(source = "http://s/arc?utc={utc}")

        launcher.play(channel, cell(withProgramme = false))

        assertNull(session.consume()?.title)
    }

    @Test
    fun `an unbuildable channel leaves the session empty and stays on the guide`() {
        launcher.play(testChannel(2, 2, "Plain"), cell())

        assertNull(session.consume())
        assertEquals(0, fullscreens)
    }

    @Test
    fun `the session is one-shot`() {
        val channel = testChannel(1, 1, "News One").withCatchup(source = "http://s/arc?utc={utc}")
        launcher.play(channel, cell())

        assertEquals(false, session.consume() == null)
        assertNull(session.consume())
    }
}
