package com.johncorser.telly.features.playlist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class M3uPlaylistParseTest {
    private val playlistBody =
        """
        #EXTM3U url-tvg="http://epg.example/guide.xml"

        #EXTINF:-1 tvg-id="one.fixture" tvg-name="News One" tvg-logo="http://logo.example/1.png" group-title="News",News One
        http://stream.example/1.ts
        # a stray comment between entries
        #EXTINF:-1,Bare Channel
        http://stream.example/2.ts
        """.trimIndent()

    @Test
    fun `parses channels with attributes and the epg hint`() {
        val playlist = M3uParser.parse(playlistBody)

        assertEquals("http://epg.example/guide.xml", playlist.epgUrl)
        val expectedFirst =
            M3uChannel(
                title = "News One",
                streamUrl = "http://stream.example/1.ts",
                tvgId = "one.fixture",
                tvgName = "News One",
                tvgLogo = "http://logo.example/1.png",
                groupTitle = "News",
            )
        assertEquals(listOf(expectedFirst.title, "Bare Channel"), playlist.channels.map { it.title })
        assertEquals(expectedFirst, playlist.channels.first())
        assertEquals(
            M3uChannel(title = "Bare Channel", streamUrl = "http://stream.example/2.ts"),
            playlist.channels.last(),
        )
    }

    @Test
    fun `playlists without a header or epg hint parse with a null epg url`() {
        val playlist = M3uParser.parse("#EXTINF:-1,Solo\nhttp://stream.example/solo.ts")

        assertNull(playlist.epgUrl)
        assertEquals(1, playlist.channels.size)
    }

    @Test
    fun `urls without a preceding extinf and trailing extinf lines are dropped`() {
        val playlist =
            M3uParser.parse(
                "#EXTM3U\nhttp://stream.example/orphan.ts\n#EXTINF:-1,No URL Follows",
            )

        assertEquals(emptyList<M3uChannel>(), playlist.channels)
    }

    @Test
    fun `header attributes are empty for non-header lines`() {
        assertEquals(emptyMap<String, String>(), M3uParser.headerAttributes("#EXTINF:-1,Nope"))
        assertEquals(
            mapOf("url-tvg" to "http://epg.example/guide.xml"),
            M3uParser.headerAttributes("#EXTM3U url-tvg=\"http://epg.example/guide.xml\""),
        )
    }

    @Test
    fun `channels and playlists behave as value objects`() {
        val channel = M3uChannel(title = "News One", streamUrl = "http://stream.example/1.ts")
        val playlist = M3uPlaylist(epgUrl = null, channels = listOf(channel))

        assertEquals(channel, channel.copy())
        assertEquals(playlist, playlist.copy())
        assertEquals(playlist.hashCode(), playlist.copy().hashCode())
        assertEquals(listOf(channel), playlist.component2())
        assertNull(playlist.component1())
        assertTrue(channel.toString().contains("News One"))
        assertEquals("News One", channel.component1())
        assertEquals("http://stream.example/1.ts", channel.component2())
        assertNull(channel.component3())
        assertNull(channel.component4())
        assertNull(channel.component5())
        assertNull(channel.component6())
    }
}
