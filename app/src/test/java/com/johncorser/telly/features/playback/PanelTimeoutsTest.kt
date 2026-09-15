package com.johncorser.telly.features.playback

import org.junit.Assert.assertEquals
import org.junit.Test

class PanelTimeoutsTest {
    @Test
    fun `the default matches today's measured constants exactly`() {
        assertEquals(5_350L, PanelTimeouts.DEFAULT.infoMs)
        assertEquals(5_500L, PanelTimeouts.DEFAULT.zapMs)
        assertEquals(5_000L, PanelTimeouts.DEFAULT.quickBarMs)
    }

    @Test
    fun `the default 5 seconds reproduces the constants`() {
        assertEquals(PanelTimeouts.DEFAULT, PanelTimeouts.forSeconds(5))
    }

    @Test
    fun `other choices keep the measured offsets over the chosen base`() {
        assertEquals(PanelTimeouts(2_350L, 2_500L, 2_000L), PanelTimeouts.forSeconds(2))
        assertEquals(PanelTimeouts(8_350L, 8_500L, 8_000L), PanelTimeouts.forSeconds(8))
        assertEquals(PanelTimeouts(10_350L, 10_500L, 10_000L), PanelTimeouts.forSeconds(10))
    }
}
