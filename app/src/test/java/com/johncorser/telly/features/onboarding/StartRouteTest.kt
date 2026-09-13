package com.johncorser.telly.features.onboarding

import com.johncorser.telly.core.navigation.Route
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StartRouteTest {
    @Test
    fun `a fresh install keeps the onboarding stack`() {
        assertNull(StartRoute.forCounts(0, 0))
        assertNull(StartRoute.forCounts(-1, 0))
    }

    @Test
    fun `persisted channels skip onboarding to the loaded screen`() {
        assertEquals(Route.ChannelsLoaded(30, 5), StartRoute.forCounts(30, 5))
    }
}
