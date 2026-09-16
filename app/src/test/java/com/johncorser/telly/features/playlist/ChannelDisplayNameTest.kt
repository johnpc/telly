package com.johncorser.telly.features.playlist

import com.johncorser.telly.features.playlist.db.ChannelOverrides
import com.johncorser.telly.features.playlist.db.displayName
import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.withOverrides
import org.junit.Assert.assertEquals
import org.junit.Test

/** The one display-name resolution point every channel surface renders. */
class ChannelDisplayNameTest {
    private val channel = testChannel(1, 1, "News One")

    @Test
    fun `the playlist name shows until a custom name is set`() {
        assertEquals("News One", channel.displayName)
        assertEquals("News Uno", channel.withOverrides(ChannelOverrides(customName = "News Uno")).displayName)
    }

    @Test
    fun `a blank custom name falls back to the playlist name`() {
        assertEquals("News One", channel.withOverrides(ChannelOverrides(customName = "")).displayName)
        assertEquals("News One", channel.withOverrides(ChannelOverrides(customName = "   ")).displayName)
    }
}
