package com.johncorser.telly.features.onboarding

import com.johncorser.telly.core.navigation.Route
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StartRouteTest {
    @Test
    fun `a fresh install keeps the onboarding stack`() {
        assertNull(StartRoute.forChannelCount(0))
        assertNull(StartRoute.forChannelCount(-1))
    }

    @Test
    fun `persisted channels skip onboarding to the loaded screen`() {
        assertEquals(Route.ChannelsLoaded(30), StartRoute.forChannelCount(30))
    }
}
