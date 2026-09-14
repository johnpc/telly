package com.johncorser.telly.features.guide

import com.johncorser.telly.features.guide.GuideTestData.at
import com.johncorser.telly.features.guide.GuideTestData.originMs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GuideWindowMathTest {
    @Test
    fun `the visible span starts at the scrolled window edge`() {
        val span = GuideWindowMath.visibleSpan(originMs, 0f, GuideGeometry.TIME_VIEWPORT_DP)

        assertEquals(originMs, span.fromMs)
        assertEquals(GuideGeometry.timeAt(GuideGeometry.TIME_VIEWPORT_DP, originMs), span.toMs)
    }

    @Test
    fun `the materialize span pads the window and stays on the half-hour grid`() {
        val visible = GuideWindowMath.visibleSpan(originMs, 480f, GuideGeometry.TIME_VIEWPORT_DP)

        val span = GuideWindowMath.materializeSpan(originMs, 480f, GuideGeometry.TIME_VIEWPORT_DP)

        assertTrue(span.fromMs <= visible.fromMs - GuideWindowMath.PREFETCH_MS)
        assertTrue(span.toMs >= visible.toMs + GuideWindowMath.PREFETCH_MS)
        assertEquals(0L, (span.fromMs - originMs) % GuideGeometry.HALF_HOUR_MS)
        assertEquals(0L, (span.toMs - originMs) % GuideGeometry.HALF_HOUR_MS)
    }

    @Test
    fun `identical scrolls quantize to the identical span`() {
        val first = GuideWindowMath.materializeSpan(originMs, 10f, GuideGeometry.TIME_VIEWPORT_DP)
        val second = GuideWindowMath.materializeSpan(originMs, 100f, GuideGeometry.TIME_VIEWPORT_DP)

        assertEquals(0L, (second.fromMs - first.fromMs) % GuideGeometry.HALF_HOUR_MS)
    }

    @Test
    fun `scroll bounds follow the past-days setting and the forward horizon`() {
        assertEquals(-7 * 48 * 160f, GuideWindowMath.scrollFloorDp(7))
        assertEquals(-48 * 160f, GuideWindowMath.scrollFloorDp(1))
        assertEquals(GuideWindowMath.FORWARD_DAYS * 48 * 160f, GuideWindowMath.scrollCeilDp())
    }

    @Test
    fun `quantizeDown floors onto the origin grid even in the past`() {
        assertEquals(at(14, 0), GuideWindowMath.quantizeDown(at(14, 29), originMs))
        assertEquals(at(13, 30), GuideWindowMath.quantizeDown(at(13, 59), originMs))
        assertEquals(at(14, 30), GuideWindowMath.quantizeDown(at(14, 30), originMs))
    }
}
