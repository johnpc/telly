package com.johncorser.telly.features.guide

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class GuideStyleTest {
    @Test
    fun `default style is exactly today's rendering`() {
        assertEquals(GuideGeometry.ROW_HEIGHT_DP, GuideStyle.DEFAULT.rowHeightDp, 0f)
        assertEquals(1f, GuideStyle.DEFAULT.backgroundAlpha, 0f)
        assertEquals(true, GuideStyle.DEFAULT.showChannelNumbers)
    }

    @Test
    fun `7 visible channels is exactly the 39 dp row pitch`() {
        assertEquals(39f, GuideStyle.rowHeightFor(7), 0f)
    }

    @Test
    fun `row pitch scales inversely with the visible channel count`() {
        assertEquals(45.5f, GuideStyle.rowHeightFor(6), 0f)
        assertEquals(273f / 8, GuideStyle.rowHeightFor(8), 0f)
        assertEquals(273f / 9, GuideStyle.rowHeightFor(9), 0f)
    }

    @Test
    fun `transparency labels map to background alphas with Opaque as 1`() {
        assertEquals(1f, GuideStyle.backgroundAlphaFor("Opaque"), 0f)
        assertEquals(0.9f, GuideStyle.backgroundAlphaFor("90%"), 0f)
        assertEquals(0.8f, GuideStyle.backgroundAlphaFor("80%"), 0f)
        assertEquals(0.7f, GuideStyle.backgroundAlphaFor("70%"), 0f)
        assertEquals(1f, GuideStyle.backgroundAlphaFor("garbage"), 0f)
    }

    @Test
    fun `from resolves the stored values into one style`() {
        val style = GuideStyle.from(visibleChannels = 6, transparency = "80%", showChannelNumbers = false)
        assertEquals(45.5f, style.rowHeightDp, 0f)
        assertEquals(0.8f, style.backgroundAlpha, 0f)
        assertFalse(style.showChannelNumbers)
        assertEquals(GuideStyle.DEFAULT, GuideStyle.from(7, "Opaque", true))
    }
}
