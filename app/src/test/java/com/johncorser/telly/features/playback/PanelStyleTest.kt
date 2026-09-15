package com.johncorser.telly.features.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PanelStyleTest {
    @Test
    fun `the 0 percent default keeps today's alphas exactly`() {
        val style = PanelStyle.from(extraTransparencyPercent = 0, showClock = true)
        assertEquals(PanelStyle.DEFAULT, style)
        assertEquals(0.68f, style.scale(0.68f), 0f)
        assertEquals(0.8f, style.scale(0.8f), 0f)
    }

    @Test
    fun `extra transparency scales today's alphas down`() {
        val style = PanelStyle.from(extraTransparencyPercent = 25, showClock = true)
        assertEquals(0.75f, style.alphaFactor, 0f)
        assertEquals(0.6f, style.scale(0.8f), 0.0001f)
        assertEquals(0.5f, PanelStyle.from(50, true).alphaFactor, 0f)
    }

    @Test
    fun `show clock carries through`() {
        assertFalse(PanelStyle.from(0, showClock = false).showClock)
    }
}
