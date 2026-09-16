package com.johncorser.telly.features.recording

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class HlsStreamRecorderTest {
    private val server = MockWebServer()
    private val sink = tempRecordingFiles().second.newFile("HLS Live", 0L)
    private val polls = mutableListOf<Long>()
    private val logs = mutableListOf<String>()

    private fun recorder(stopAfterPolls: Int = Int.MAX_VALUE): Pair<HlsStreamRecorder, () -> Boolean> {
        val recorder =
            HlsStreamRecorder(
                http = HlsClient(),
                pollDelay = { polls += it },
                log = { logs += it },
            )
        return recorder to { polls.size >= stopAfterPolls }
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun url(path: String): String = server.url(path).toString()

    private fun enqueue(vararg bodies: String) {
        bodies.forEach { server.enqueue(MockResponse().setBody(it)) }
    }

    @Test
    fun `a master playlist records its highest-bandwidth variant's segments until ENDLIST`() =
        runTest {
            enqueue(
                "#EXTM3U\n#EXT-X-STREAM-INF:BANDWIDTH=100000\nlo/index.m3u8\n" +
                    "#EXT-X-STREAM-INF:BANDWIDTH=900000\nhi/index.m3u8\n",
                "#EXTM3U\n#EXT-X-TARGETDURATION:2\n#EXTINF:2.0,\nseg0.ts\n#EXTINF:2.0,\nseg1.ts\n#EXT-X-ENDLIST\n",
                "AAAA",
                "BB",
            )
            server.start()
            val (recorder, stop) = recorder()

            val written = recorder.copy(url("/live/master.m3u8"), sink, stop)

            assertEquals(6L, written)
            assertEquals("AAAABB", sink.readText())
            val paths = List(4) { server.takeRequest() }
            assertEquals(
                listOf("/live/master.m3u8", "/live/hi/index.m3u8", "/live/hi/seg0.ts", "/live/hi/seg1.ts"),
                paths.map { it.path },
            )
            assertTrue(paths.all { it.getHeader("User-Agent")!!.contains("SHIELD Android TV") })
            assertTrue(polls.isEmpty())
        }

    @Test
    fun `a reconnect attempt dedupes segments already on disk by sequence`() =
        runTest {
            val media = "#EXTM3U\n#EXTINF:2.0,\nseg0.ts\n#EXTINF:2.0,\nseg1.ts\n#EXT-X-ENDLIST\n"
            enqueue(media, "AAAA", "BB", media)
            server.start()
            val (recorder, stop) = recorder()

            assertEquals(6L, recorder.copy(url("/live/index.m3u8"), sink, stop))
            assertEquals(0L, recorder.copy(url("/live/index.m3u8"), sink, stop))

            assertEquals("AAAABB", sink.readText())
        }

    @Test
    fun `a live playlist polls per target duration, skips missed sequences and stops on demand`() =
        runTest {
            enqueue(
                "#EXTM3U\n#EXT-X-TARGETDURATION:2\n#EXT-X-MEDIA-SEQUENCE:0\n" +
                    "#EXTINF:2.0,\nseg0.ts\n#EXTINF:2.0,\nseg1.ts\n",
                "AA",
                "BB",
                "#EXTM3U\n#EXT-X-TARGETDURATION:2\n#EXT-X-MEDIA-SEQUENCE:3\n#EXTINF:2.0,\nseg3.ts\n",
                "DD",
            )
            server.start()
            val (recorder, stop) = recorder(stopAfterPolls = 2)

            val written = recorder.copy(url("/live/index.m3u8"), sink, stop)

            assertEquals(6L, written)
            assertEquals("AABBDD", sink.readText())
            assertEquals(listOf(2_000L, 2_000L), polls)
            assertTrue(logs.single().contains("missed segments 2..2"))
        }

    @Test
    fun `fMP4 playlists get the init segment first, exactly once`() =
        runTest {
            val media =
                "#EXTM3U\n#EXT-X-MAP:URI=\"init.mp4\"\n#EXTINF:2.0,\nseg0.m4s\n#EXT-X-ENDLIST\n"
            enqueue(media, "INIT", "SEG0", media)
            server.start()
            val (recorder, stop) = recorder()

            assertEquals(8L, recorder.copy(url("/live/index.m3u8"), sink, stop))
            assertEquals(0L, recorder.copy(url("/live/index.m3u8"), sink, stop))

            assertEquals("INITSEG0", sink.readText())
        }

    @Test(expected = IOException::class)
    fun `a playlist failure with no progress throws so the engine counts an idle attempt`() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(503))
            server.start()
            val (recorder, stop) = recorder()

            recorder.copy(url("/live/index.m3u8"), sink, stop)
        }

    @Test(expected = IOException::class)
    fun `a master playlist without variants is a failed attempt`() =
        runTest {
            enqueue("#EXTM3U\n#EXT-X-STREAM-INF:BANDWIDTH=100000\n")
            server.start()
            val (recorder, stop) = recorder()

            recorder.copy(url("/live/master.m3u8"), sink, stop)
        }

    @Test
    fun `a failure after real progress reports the bytes and defers the reconnect`() =
        runTest {
            server.dispatcher =
                object : Dispatcher() {
                    var requests = 0

                    override fun dispatch(request: RecordedRequest): MockResponse =
                        when (requests++) {
                            0 -> MockResponse().setBody("#EXTM3U\n#EXT-X-TARGETDURATION:2\n#EXTINF:2.0,\nseg0.ts\n")
                            1 -> MockResponse().setBody("AA")
                            else -> MockResponse().setResponseCode(503)
                        }
                }
            server.start()
            val (recorder, stop) = recorder()

            val written = recorder.copy(url("/live/index.m3u8"), sink, stop)

            assertEquals(2L, written)
            assertEquals("AA", sink.readText())
            assertTrue(logs.single().contains("reconnecting"))
        }

    @Test
    fun `an already-satisfied stop copies nothing`() =
        runTest {
            enqueue("#EXTM3U\n#EXTINF:2.0,\nseg0.ts\n")
            server.start()
            val (recorder, _) = recorder()

            assertEquals(0L, recorder.copy(url("/live/index.m3u8"), sink) { true })
            assertEquals(0L, sink.length())
        }
}
