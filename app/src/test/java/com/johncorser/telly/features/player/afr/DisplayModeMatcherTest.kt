package com.johncorser.telly.features.player.afr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The AFR mode-choice rules, including the NTSC fuzzy matches. */
class DisplayModeMatcherTest {
    // A Shield-style 4K mode set: no 25/30 Hz modes, fractional NTSC rates.
    private val uhd60 = AfrMode(1, 3840, 2160, 60f)
    private val uhd5994 = AfrMode(2, 3840, 2160, 59.94f)
    private val uhd50 = AfrMode(3, 3840, 2160, 50f)
    private val uhd24 = AfrMode(4, 3840, 2160, 24f)
    private val uhd23976 = AfrMode(5, 3840, 2160, 23.976f)
    private val fhd60 = AfrMode(6, 1920, 1080, 60f)
    private val all = listOf(uhd60, uhd5994, uhd50, uhd24, uhd23976, fhd60)

    private fun from(current: AfrMode) = AfrModeSet(current, all)

    @Test
    fun `film content picks the 24 Hz mode from 60`() {
        assertEquals(uhd24.id, DisplayModeMatcher.bestModeId(from(uhd60), 24f))
    }

    @Test
    fun `NTSC film 23-976 prefers the fractional 23-976 mode over 24`() {
        assertEquals(uhd23976.id, DisplayModeMatcher.bestModeId(from(uhd60), 23.976f))
    }

    @Test
    fun `PAL 25 fps doubles to 50 Hz because no 25 Hz mode exists`() {
        assertEquals(uhd50.id, DisplayModeMatcher.bestModeId(from(uhd60), 25f))
    }

    @Test
    fun `NTSC 29-97 fps doubles to 59-94 preferred over 60`() {
        assertEquals(uhd5994.id, DisplayModeMatcher.bestModeId(from(uhd50), 29.97f))
    }

    @Test
    fun `30 fps doubles to 60 Hz preferred over 59-94`() {
        assertEquals(uhd60.id, DisplayModeMatcher.bestModeId(from(uhd50), 30f))
    }

    @Test
    fun `a current mode that already matches means no switch`() {
        assertNull(DisplayModeMatcher.bestModeId(from(uhd60), 30f))
        assertNull(DisplayModeMatcher.bestModeId(from(uhd5994), 29.97f))
        assertNull(DisplayModeMatcher.bestModeId(from(uhd24), 23.976f))
    }

    @Test
    fun `only modes at the current resolution are candidates`() {
        val fhdOnly60 = AfrModeSet(fhd60, all)
        // 24 Hz exists only at UHD; FHD stays put.
        assertNull(DisplayModeMatcher.bestModeId(fhdOnly60, 24f))
    }

    @Test
    fun `the smallest integer multiple wins`() {
        val with48 = AfrModeSet(uhd60, all + AfrMode(7, 3840, 2160, 48f))
        assertEquals(uhd24.id, DisplayModeMatcher.bestModeId(with48, 24f))
    }

    @Test
    fun `unknown or absurd rates match nothing`() {
        assertNull(DisplayModeMatcher.bestModeId(from(uhd60), 0f))
        assertNull(DisplayModeMatcher.bestModeId(from(uhd60), -1f))
        assertNull(DisplayModeMatcher.bestModeId(from(uhd24), 17.3f))
    }

    @Test
    fun `multipleOf spans the fuzzy table`() {
        assertEquals(1, DisplayModeMatcher.multipleOf(24f, 23.976f))
        assertEquals(2, DisplayModeMatcher.multipleOf(50f, 25f))
        assertEquals(2, DisplayModeMatcher.multipleOf(59.94f, 29.97f))
        assertEquals(2, DisplayModeMatcher.multipleOf(60f, 29.97f))
        assertNull(DisplayModeMatcher.multipleOf(50f, 24f))
    }
}
