package com.johncorser.telly.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigatorTest {
    @Test
    fun `starts at the welcome route`() {
        val navigator = Navigator()

        assertEquals(listOf(Route.Welcome), navigator.stack.value)
        assertEquals(Route.Welcome, navigator.current)
    }

    @Test
    fun `push stacks routes in order`() {
        val navigator = Navigator()

        navigator.push(Route.Settings)
        navigator.push(Route.AddPlaylistWizard)

        assertEquals(listOf(Route.Welcome, Route.Settings, Route.AddPlaylistWizard), navigator.stack.value)
        assertEquals(Route.AddPlaylistWizard, navigator.current)
    }

    @Test
    fun `pop removes the top route and reports success`() {
        val navigator = Navigator()
        navigator.push(Route.Settings)

        assertTrue(navigator.pop())

        assertEquals(listOf(Route.Welcome), navigator.stack.value)
    }

    @Test
    fun `pop at the root does nothing and reports failure`() {
        val navigator = Navigator()

        assertFalse(navigator.pop())

        assertEquals(listOf(Route.Welcome), navigator.stack.value)
    }

    @Test
    fun `replaceAll clears history and installs a new root`() {
        val navigator = Navigator()
        navigator.push(Route.AddPlaylistWizard)

        navigator.replaceAll(Route.ChannelsLoaded(channelCount = 30, groupCount = 5))

        assertEquals(listOf(Route.ChannelsLoaded(channelCount = 30, groupCount = 5)), navigator.stack.value)
        assertFalse(navigator.pop())
    }

    @Test
    fun `channels loaded routes behave as value objects`() {
        val route = Route.ChannelsLoaded(channelCount = 5, groupCount = 2)

        assertEquals(5, route.channelCount)
        assertEquals(route, route.copy())
        assertNotEquals(route, route.copy(channelCount = 6))
        assertEquals(2, route.groupCount)
        assertEquals(route.hashCode(), Route.ChannelsLoaded(5, 2).hashCode())
        assertTrue(route.toString().contains("5"))
    }
}
