package com.johncorser.telly.features.vod

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VodResumePolicyTest {
    @Test
    fun `positions between 5 and 95 percent offer resume`() {
        assertTrue(VodResumePolicy.offerResume(positionMs = 5_000, durationMs = 100_000))
        assertTrue(VodResumePolicy.offerResume(positionMs = 50_000, durationMs = 100_000))
        assertTrue(VodResumePolicy.offerResume(positionMs = 95_000, durationMs = 100_000))
    }

    @Test
    fun `positions outside the band do not offer resume`() {
        assertFalse(VodResumePolicy.offerResume(positionMs = 4_999, durationMs = 100_000))
        assertFalse(VodResumePolicy.offerResume(positionMs = 95_100, durationMs = 100_000))
        assertFalse(VodResumePolicy.offerResume(positionMs = 10_000, durationMs = 0))
    }

    @Test
    fun `past 95 percent counts as finished`() {
        assertTrue(VodResumePolicy.finished(positionMs = 96_000, durationMs = 100_000))
        assertFalse(VodResumePolicy.finished(positionMs = 95_000, durationMs = 100_000))
        assertFalse(VodResumePolicy.finished(positionMs = 96_000, durationMs = 0))
    }
}
