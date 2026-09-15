package com.johncorser.telly.features.vod

import org.junit.Assert.assertEquals
import org.junit.Test

class VodTimesTest {
    @Test
    fun `under an hour renders minutes and seconds`() {
        assertEquals("0:00", VodTimes.format(0))
        assertEquals("0:09", VodTimes.format(9_400))
        assertEquals("59:59", VodTimes.format(3_599_000))
    }

    @Test
    fun `an hour and above adds the hour figure`() {
        assertEquals("1:00:00", VodTimes.format(3_600_000))
        assertEquals("2:05:07", VodTimes.format(7_507_000))
    }

    @Test
    fun `negative inputs clamp to zero`() {
        assertEquals("0:00", VodTimes.format(-5_000))
    }
}
