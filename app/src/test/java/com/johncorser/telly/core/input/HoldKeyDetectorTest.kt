package com.johncorser.telly.core.input

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HoldKeyDetectorTest {
    private val detector = HoldKeyDetector("TAP", "HOLD")

    @Test
    fun `a short press fires the tap on release`() {
        assertNull(detector.onDown(0))
        assertEquals("TAP", detector.onUp())
    }

    @Test
    fun `a held press fires the hold once and swallows the release`() {
        assertNull(detector.onDown(0))
        assertEquals("HOLD", detector.onDown(1))
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
        assertEquals("TAP", detector.onUp())
    }

    @Test
    fun `a null hold swallows the whole long press`() {
        val tapOnly = HoldKeyDetector("TAP", null)
        assertNull(tapOnly.onDown(0))
        assertNull(tapOnly.onDown(1))
        assertNull(tapOnly.onUp())
        assertNull(tapOnly.onDown(0))
        assertEquals("TAP", tapOnly.onUp())
    }
}
