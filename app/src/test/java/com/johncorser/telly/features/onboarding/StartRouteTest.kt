package com.johncorser.telly.features.onboarding

import com.johncorser.telly.core.navigation.Route
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StartRouteTest {
    @Test
    fun `a fresh install lands on the welcome flow once the count resolves`() {
        val start = StartRoute()
        assertEquals(StartDecision.Welcome, start.decide(0))
        assertEquals(Route.Welcome, start.forChannelCount(-1))
        assertTrue(start.guideTunesPreview())
    }

    @Test
    fun `persisted channels go straight to playback by default (last channel on start)`() {
        val start = StartRoute()
        assertEquals(Route.Playback, start.forChannelCount(30))
        assertTrue(start.guideTunesPreview())
    }

    @Test
    fun `last channel on start OFF cold-starts at the guide, untuned exactly once`() {
        val start = StartRoute(lastChannelOnStart = { false })

        assertEquals(Route.Guide, start.forChannelCount(30))

        assertFalse("the cold-start guide skips its preview tune", start.guideTunesPreview())
        assertTrue("later guide entries resume the preview", start.guideTunesPreview())
        assertNull("the decision was handed over exactly once", start.claimForGuide())
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
        assertTrue(start.guideTunesPreview())
    }

    @Test
    fun `a later decision replaces an unclaimed one`() {
        var on = false
        val start = StartRoute(lastChannelOnStart = { on })
        start.forChannelCount(5) // untuned guide decided, nobody claimed it
        on = true
        start.forChannelCount(5) // e.g. a restore landed on playback

        assertTrue(start.guideTunesPreview())
    }
}
