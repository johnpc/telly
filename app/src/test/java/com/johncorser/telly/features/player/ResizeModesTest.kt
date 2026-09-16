package com.johncorser.telly.features.player

import androidx.media3.ui.AspectRatioFrameLayout
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ResizeModesTest {
    @Test
    fun `the captured default Fit letterboxes`() {
        assertEquals(AspectRatioFrameLayout.RESIZE_MODE_FIT, ResizeModes.of("Fit"))
    }

    @Test
    fun `stretch and fill map to FILL`() {
        assertEquals(AspectRatioFrameLayout.RESIZE_MODE_FILL, ResizeModes.of("Stretch"))
        assertEquals(AspectRatioFrameLayout.RESIZE_MODE_FILL, ResizeModes.of("fill"))
    }

    @Test
    fun `crop and zoom map to ZOOM`() {
        assertEquals(AspectRatioFrameLayout.RESIZE_MODE_ZOOM, ResizeModes.of("Crop"))
        assertEquals(AspectRatioFrameLayout.RESIZE_MODE_ZOOM, ResizeModes.of("Zoom"))
    }

    @Test
    fun `fixed width and height map to their modes`() {
        assertEquals(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH, ResizeModes.of("Fixed width"))
        assertEquals(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT, ResizeModes.of("Fixed height"))
    }

    @Test
    fun `unknown values fall back to Fit - the previous hardcoded behavior`() {
        assertEquals(AspectRatioFrameLayout.RESIZE_MODE_FIT, ResizeModes.of(""))
        assertEquals(AspectRatioFrameLayout.RESIZE_MODE_FIT, ResizeModes.of("nonsense"))
    }
}
