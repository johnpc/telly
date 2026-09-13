package com.johncorser.telly.features.playback

import com.johncorser.telly.testutil.testChannel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChannelZapperTest {
    private val channels =
        listOf(
            testChannel(1, 1, "News One"),
            testChannel(2, 2, "News Two"),
            testChannel(3, 3, "Sports Arena"),
        )

    @Test
    fun `restore prefers the remembered channel`() {
        assertEquals(2L, ChannelZapper.restore(channels, 2)?.id)
    }

    @Test
    fun `restore falls back to the first channel`() {
        assertEquals(1L, ChannelZapper.restore(channels, null)?.id)
        assertEquals(1L, ChannelZapper.restore(channels, 99)?.id)
        assertNull(ChannelZapper.restore(emptyList(), 1))
    }

    @Test
    fun `neighbour wraps in both directions`() {
        assertEquals(2L, ChannelZapper.neighbour(channels, channels[0], +1)?.id)
        assertEquals(1L, ChannelZapper.neighbour(channels, channels[2], +1)?.id)
        assertEquals(3L, ChannelZapper.neighbour(channels, channels[0], -1)?.id)
        assertEquals(1L, ChannelZapper.neighbour(channels, channels[0], 0)?.id)
    }

    @Test
    fun `neighbour of a vanished or missing channel is the first`() {
        assertEquals(1L, ChannelZapper.neighbour(channels, testChannel(9, 9, "Gone"), +1)?.id)
        assertEquals(1L, ChannelZapper.neighbour(channels, null, +1)?.id)
        assertNull(ChannelZapper.neighbour(emptyList(), channels[0], +1))
    }
}
