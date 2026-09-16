package com.johncorser.telly.features.onboarding

import com.johncorser.telly.core.navigation.Route
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StartRouteTest {
    @Test
    fun `a fresh install lands on the welcome flow once the count resolves`() {
        val start = StartRoute()
        assertEquals(Route.Welcome, start.forChannelCount(0))
        assertEquals(Route.Welcome, start.forChannelCount(-1))
        assertFalse(start.consumeUntunedStart())
    }

    @Test
    fun `persisted channels go straight to playback by default (last channel on start)`() {
        val start = StartRoute()
        assertEquals(Route.Playback, start.forChannelCount(30))
        assertFalse(start.consumeUntunedStart())
    }

    @Test
    fun `last channel on start OFF cold-starts at the guide, untuned exactly once`() {
        val start = StartRoute(lastChannelOnStart = { false })

        assertEquals(Route.Guide, start.forChannelCount(30))

        assertTrue(start.consumeUntunedStart())
        assertFalse("one-shot: later guide entries resume the preview", start.consumeUntunedStart())
    }

    @Test
    fun `the setting is read per cold start, not captured`() {
        var on = false
        val start = StartRoute(lastChannelOnStart = { on })
        assertEquals(Route.Guide, start.forChannelCount(5))
        on = true
        assertEquals(Route.Playback, start.forChannelCount(5))
    }

    @Test
    fun `a fresh install never routes to the guide even with the toggle off`() {
        val start = StartRoute(lastChannelOnStart = { false })
        assertEquals(Route.Welcome, start.forChannelCount(0))
        assertFalse(start.consumeUntunedStart())
    }
}
