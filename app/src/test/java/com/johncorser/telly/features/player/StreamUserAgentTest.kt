package com.johncorser.telly.features.player

import org.junit.Assert.assertEquals
import org.junit.Test

class StreamUserAgentTest {
    private val resolved = mutableListOf<String>()

    private val userAgent =
        StreamUserAgent(
            resolve = { url ->
                resolved += url
                "agent-for:$url"
            },
            default = "default-agent",
        )

    @Test
    fun `before any load the default applies and nothing resolves`() {
        assertEquals("default-agent", userAgent.current())
        assertEquals(emptyList<String>(), resolved)
    }

    @Test
    fun `requests resolve against the loaded stream url, segment urls included`() {
        userAgent.onLoad("http://s/1.m3u8")

        // Manifest and segment requests both ask while 1.m3u8 is tuned.
        assertEquals("agent-for:http://s/1.m3u8", userAgent.current())
        assertEquals("agent-for:http://s/1.m3u8", userAgent.current())
        assertEquals(listOf("http://s/1.m3u8"), resolved)
    }

    @Test
    fun `zapping to another channel re-resolves once`() {
        userAgent.onLoad("http://s/1.m3u8")
        userAgent.current()
        userAgent.onLoad("http://s/2.m3u8")

        assertEquals("agent-for:http://s/2.m3u8", userAgent.current())
        assertEquals(listOf("http://s/1.m3u8", "http://s/2.m3u8"), resolved)
    }
}
