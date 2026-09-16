package com.johncorser.telly.features.recording

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class HlsContainerProbeTest {
    private val server = MockWebServer()
    private val probe = HlsContainerProbe()

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `non-HLS sources are ts captures without any probe request`() =
        runTest {
            server.start()

            assertEquals("ts", probe.extensionOf(server.url("/streams/news.ts").toString()))
            assertEquals(0, server.requestCount)
        }

    @Test
    fun `TS-segment HLS stays a ts capture`() =
        runTest {
            server.enqueue(MockResponse().setBody("#EXTM3U\n#EXTINF:2.0,\nseg0.ts\n"))
            server.start()

            assertEquals("ts", probe.extensionOf(server.url("/live/index.m3u8").toString()))
        }

    @Test
    fun `fMP4 HLS becomes an mp4 capture, hopping master playlists first`() =
        runTest {
            server.enqueue(MockResponse().setBody("#EXTM3U\n#EXT-X-STREAM-INF:BANDWIDTH=1\nmedia.m3u8\n"))
            server.enqueue(MockResponse().setBody("#EXTM3U\n#EXT-X-MAP:URI=\"init.mp4\"\n#EXTINF:2.0,\nseg0.m4s\n"))
            server.start()

            assertEquals("mp4", probe.extensionOf(server.url("/live/master.m3u8").toString()))
        }

    @Test
    fun `a probe failure falls back to ts`() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(503))
            server.start()

            assertEquals("ts", probe.extensionOf(server.url("/live/index.m3u8").toString()))
        }
}
