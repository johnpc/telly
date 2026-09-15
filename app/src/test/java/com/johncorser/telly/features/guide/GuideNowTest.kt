package com.johncorser.telly.features.guide

import com.johncorser.telly.features.guide.GuideTestData.at
import com.johncorser.telly.features.playback.PlaybackTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GuideNowTest {
    @Test
    fun `seeds from the injected clock and re-samples on each tick and reseed`() {
        runTest {
            var clock = at(14, 38)
            val ticks = MutableSharedFlow<Unit>()
            val guideNow = GuideNow({ clock }, backgroundScope, ticks)
            runCurrent()
            assertEquals(at(14, 38), guideNow.now.value)

            clock = at(14, 39)
            ticks.emit(Unit)
            runCurrent()
            assertEquals(at(14, 39), guideNow.now.value)

            clock = at(15, 12)
            guideNow.reseed()
            assertEquals(at(15, 12), guideNow.now.value)
        }
    }

    @Test
    fun `minute boundary ticks fire when the injected clock's minute turns`() {
        runTest {
            // 14:38:00.000 — each of the next two boundaries is 60 s away.
            val clock = { at(14, 38) + currentTime }
            var ticked = 0
            val collector = launch { PlaybackTime.minuteBoundaryTicks(clock).take(2).collect { ticked += 1 } }

            testScheduler.advanceTimeBy(PlaybackTime.MINUTE_MS - 1)
            assertEquals(0, ticked)
            testScheduler.advanceTimeBy(2)
            assertEquals(1, ticked)
            testScheduler.advanceTimeBy(PlaybackTime.MINUTE_MS)
            assertEquals(2, ticked)
            collector.join()
        }
    }
}
