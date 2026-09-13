package com.johncorser.telly.features.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OkLongPressDetectorTest {
    private val detector = OkLongPressDetector()

    @Test
    fun `a short press fires OK on release`() {
        assertNull(detector.onDown(0))
        assertEquals(PlaybackKey.OK, detector.onUp())
    }

    @Test
    fun `a held press fires LONG_OK once and swallows the release`() {
        assertNull(detector.onDown(0))
        assertEquals(PlaybackKey.LONG_OK, detector.onDown(1))
        assertNull(detector.onDown(2))
        assertNull(detector.onDown(3))
        assertNull(detector.onUp())
    }

    @Test
    fun `the detector resets between presses`() {
        detector.onDown(0)
        detector.onDown(1)
        detector.onUp()
        assertNull(detector.onDown(0))
        assertEquals(PlaybackKey.OK, detector.onUp())
    }
}
