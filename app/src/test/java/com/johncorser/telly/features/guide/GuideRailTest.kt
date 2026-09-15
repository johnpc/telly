package com.johncorser.telly.features.guide

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The rail's vertical chain: search -> live-TV -> gear, ends stop dead. */
class GuideRailTest {
    @Test
    fun `stops chain top to bottom`() {
        assertEquals(
            listOf(GuideRailStop.SEARCH, GuideRailStop.LIVE_TV, GuideRailStop.GEAR),
            GuideRailStop.entries.toList(),
        )
        assertEquals(GuideRailStop.LIVE_TV, GuideRailStop.SEARCH.below())
        assertEquals(GuideRailStop.GEAR, GuideRailStop.LIVE_TV.below())
        assertEquals(GuideRailStop.LIVE_TV, GuideRailStop.GEAR.above())
        assertEquals(GuideRailStop.SEARCH, GuideRailStop.LIVE_TV.above())
    }

    @Test
    fun `the chain ends stop dead`() {
        assertNull(GuideRailStop.SEARCH.above())
        assertNull(GuideRailStop.GEAR.below())
    }
}
