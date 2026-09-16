package com.johncorser.telly.features.guide

import com.johncorser.telly.features.guide.GuideTestData.at
import com.johncorser.telly.features.guide.GuideTestData.nowMs
import com.johncorser.telly.features.guide.GuideTestData.originMs
import com.johncorser.telly.features.guide.GuideTestData.utc
import com.johncorser.telly.features.playback.ClockStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GuideTimelineTest {
    private val viewport = GuideGeometry.TIME_VIEWPORT_DP

    @Test
    fun `ticks land every 160 dp with the captured 12-hour labels`() {
        val ticks = GuideTimeline.ticks(originMs, 0f, viewport, ClockStyle(utc))

        assertEquals(listOf(0f, 160f, 320f, 480f, 640f), ticks.map { it.offsetDp })
        assertEquals(listOf("02:30 PM", "03:00 PM", "03:30 PM", "04:00 PM", "04:30 PM"), ticks.map { it.label })
    }

    @Test
    fun `ticks pan with the scroll`() {
        val ticks = GuideTimeline.ticks(originMs, 80f, viewport, ClockStyle(utc))

        assertEquals("03:00 PM", ticks.first().label)
        assertEquals(80f, ticks.first().offsetDp)
    }

    @Test
    fun `the 24-hour clock format renders bare labels`() {
        val ticks = GuideTimeline.ticks(originMs, 0f, viewport, ClockStyle(utc) { true })

        assertEquals(listOf("14:30", "15:00", "15:30", "16:00", "16:30"), ticks.map { it.label })
    }

    @Test
    fun `the now-line sits eight minutes past the origin`() {
        val offset = GuideTimeline.nowLineOffset(nowMs, originMs, 0f, viewport)

        assertEquals(8 * 160f / 30f, offset)
    }

    @Test
    fun `the now-line disappears once scrolled away`() {
        assertNull(GuideTimeline.nowLineOffset(nowMs, originMs, 480f, viewport))
        assertNull(GuideTimeline.nowLineOffset(at(9, 0), originMs, 0f, viewport))
    }
}
