package com.johncorser.telly.features.playlist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaylistUserAgentsTest {
    private val perPlaylist = mutableMapOf<String, String>()
    private var global = ""

    private val precedence =
        UserAgentPrecedence(
            perPlaylist = { perPlaylist[it].orEmpty() },
            global = { global },
        )

    @Test
    fun `the playlist's own user-agent wins over the global setting`() {
        perPlaylist["http://p/a.m3u"] = "playlist-agent"
        global = "global-agent"

        assertEquals("playlist-agent", precedence.forPlaylist("http://p/a.m3u"))
    }

    @Test
    fun `a blank per-playlist value falls back to the global setting`() {
        perPlaylist["http://p/a.m3u"] = "  "
        global = "global-agent"

        assertEquals("global-agent", precedence.forPlaylist("http://p/a.m3u"))
        assertEquals("global-agent", precedence.forPlaylist(null))
    }

    @Test
    fun `nothing set resolves to null so the caller's default applies`() {
        assertNull(precedence.forPlaylist("http://p/a.m3u"))
        assertNull(precedence.forPlaylist(null))
    }

    @Test
    fun `stream resolution maps the stream url to its playlist's agent`() {
        perPlaylist["http://p/a.m3u"] = "playlist-agent"
        val resolver =
            StreamUserAgentResolver(
                playlistUrlFor = { if (it == "http://s/1.ts") "http://p/a.m3u" else null },
                precedence = precedence,
                fallback = "default-agent",
            )

        assertEquals("playlist-agent", resolver.resolve("http://s/1.ts"))
        assertEquals("default-agent", resolver.resolve("http://s/unknown.ts"))
    }

    @Test
    fun `an unknown stream still honors the global setting before the fallback`() {
        global = "global-agent"
        val resolver =
            StreamUserAgentResolver(
                playlistUrlFor = { null },
                precedence = precedence,
                fallback = "default-agent",
            )

        assertEquals("global-agent", resolver.resolve("http://s/unknown.ts"))
    }
}
