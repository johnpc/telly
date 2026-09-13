package com.johncorser.telly.features.onboarding

import com.johncorser.telly.core.navigation.Route
import org.junit.Assert.assertEquals
import org.junit.Test

class StartRouteTest {
    @Test
    fun `a fresh install lands on the welcome flow once the count resolves`() {
        assertEquals(Route.Welcome, StartRoute.forChannelCount(0))
        assertEquals(Route.Welcome, StartRoute.forChannelCount(-1))
    }

    @Test
    fun `persisted channels skip onboarding straight to playback`() {
        assertEquals(Route.Playback, StartRoute.forChannelCount(30))
    }
}
