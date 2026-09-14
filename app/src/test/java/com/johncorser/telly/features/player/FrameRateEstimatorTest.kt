package com.johncorser.telly.features.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FrameRateEstimatorTest {
    @Test
    fun `a steady 25fps stream estimates after enough samples`() {
        val estimator = FrameRateEstimator(samples = 4)

        assertNull(estimator.onFrame(0))
        assertNull(estimator.onFrame(40_000))
        assertNull(estimator.onFrame(80_000))
        assertNull(estimator.onFrame(120_000))
        assertEquals(25f, estimator.onFrame(160_000)!!, 0.01f)
    }

    @Test
    fun `a single late frame does not skew the median`() {
        val estimator = FrameRateEstimator(samples = 5)
        estimator.onFrame(0)
        var atUs = 0L
        var result: Float? = null
        listOf(40_000L, 40_000L, 120_000L, 40_000L, 40_000L).forEach { delta ->
            atUs += delta
            result = estimator.onFrame(atUs)
        }
        assertEquals(25f, result!!, 0.01f)
    }

    @Test
    fun `a backwards timestamp resets the window`() {
        val estimator = FrameRateEstimator(samples = 2)
        estimator.onFrame(100_000)
        estimator.onFrame(140_000)

        assertNull(estimator.onFrame(50_000))

        assertNull(estimator.onFrame(90_000))
        assertEquals(25f, estimator.onFrame(130_000)!!, 0.01f)
    }

    @Test
    fun `sixty fps rounds to the badge value`() {
        val estimator = FrameRateEstimator(samples = 3)
        var result: Float? = null
        for (frame in 1..4) {
            result = estimator.onFrame(frame * 16_667L)
        }
        assertEquals(60f, result!!, 0.1f)
    }
}
