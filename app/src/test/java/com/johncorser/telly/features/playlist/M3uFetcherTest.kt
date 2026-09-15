package com.johncorser.telly.features.playlist

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

class M3uFetcherTest {
    private val server = MockWebServer()
    private val fetcher = M3uFetcher()

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `returns the playlist body on http 200`() =
        runTest {
            server.enqueue(MockResponse().setBody("#EXTM3U\n#EXTINF:-1,One\nhttp://s/1.ts"))
            server.start()

            val body = fetcher.fetch(server.url("/playlist.m3u").toString())

            assertEquals("#EXTM3U\n#EXTINF:-1,One\nhttp://s/1.ts", body)
            assertEquals("/playlist.m3u", server.takeRequest().path)
        }

    @Test
    fun `sends the resolved user-agent and none when unresolved`() =
        runTest {
            server.enqueue(MockResponse().setBody("#EXTM3U"))
            server.enqueue(MockResponse().setBody("#EXTM3U"))
            server.start()
            val withAgent =
                M3uFetcher(userAgentFor = { url -> "telly-agent".takeIf { url.contains("agent") } })

            withAgent.fetch(server.url("/agent.m3u").toString())
            withAgent.fetch(server.url("/plain.m3u").toString())

            assertEquals("telly-agent", server.takeRequest().getHeader("User-Agent"))
            assertTrue(server.takeRequest().getHeader("User-Agent").orEmpty().startsWith("okhttp"))
        }

    @Test
    fun `throws an io exception on http errors`() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(404))
            server.start()

            try {
                fetcher.fetch(server.url("/missing.m3u").toString())
                fail("Expected an IOException for HTTP 404")
            } catch (expected: IOException) {
                assertTrue(expected.message.orEmpty().contains("404"))
            }
        }
}
