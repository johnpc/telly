package com.johncorser.telly.features.recording

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

class OkHttpStreamRecorderTest {
    private val server = MockWebServer()
    private val recorder = OkHttpStreamRecorder()
    private val sink = tempRecordingFiles().second.newFile("News One", 0L)

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `copies the response body onto the sink with the default stream user-agent`() =
        runTest {
            server.enqueue(MockResponse().setBody("tsbytes-tsbytes"))
            server.start()

            val written = recorder.copy(server.url("/streams/news.ts").toString(), sink) { false }

            assertEquals(15L, written)
            assertEquals("tsbytes-tsbytes", sink.readText())
            val request = server.takeRequest()
            assertTrue(request.getHeader("User-Agent")!!.contains("SHIELD Android TV"))
        }

    @Test
    fun `sends the user-agent the injected resolver picks for the stream url`() =
        runTest {
            // The locator injects streamUserAgentFor (per-playlist > global >
            // default) — the recorder must send whatever it resolves for the
            // EXACT url being captured, exactly like the player does.
            server.enqueue(MockResponse().setBody("ts"))
            server.start()
            val url = server.url("/streams/news.ts").toString()
            val resolved = mutableListOf<String>()
            val recorder =
                OkHttpStreamRecorder(
                    userAgentFor = { streamUrl ->
                        resolved += streamUrl
                        "playlist-ua/1.0"
                    },
                )

            recorder.copy(url, sink) { false }

            assertEquals(listOf(url), resolved)
            assertEquals("playlist-ua/1.0", server.takeRequest().getHeader("User-Agent"))
        }

    @Test
    fun `appends across attempts and follows redirects`() =
        runTest {
            server.enqueue(
                MockResponse()
                    .setResponseCode(302)
                    .setHeader("Location", "/moved.ts"),
            )
            server.enqueue(MockResponse().setBody("abc"))
            server.start()
            sink.writeText("xyz")

            val written = recorder.copy(server.url("/original.ts").toString(), sink) { false }

            assertEquals(3L, written)
            assertEquals("xyzabc", sink.readText())
        }

    @Test(expected = IOException::class)
    fun `a non-success response throws so the engine counts a failed attempt`() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(503))
            server.start()

            recorder.copy(server.url("/down.ts").toString(), sink) { false }
        }

    @Test
    fun `a user stop cancels a trickling stream read instead of waiting it out`() =
        runTest {
            // One byte per 3 s: the trickle keeps resetting the read timeout
            // and the between-read stop check stays parked inside read() —
            // only the stop-cancel can land in under one throttle period.
            server.enqueue(MockResponse().setBody("XXXX").throttleBody(1, 3, TimeUnit.SECONDS))
            server.start()
            val stopAtMs = System.currentTimeMillis() + 500

            val elapsedMs =
                measureTimeMillis {
                    recorder.copy(server.url("/streams/slow.ts").toString(), sink) {
                        System.currentTimeMillis() >= stopAtMs
                    }
                }

            assertTrue("stop took ${elapsedMs}ms to land", elapsedMs < 2_000)
        }

    @Test
    fun `an already-satisfied stop copies nothing`() =
        runTest {
            server.enqueue(MockResponse().setBody("tsbytes"))
            server.start()

            val written = recorder.copy(server.url("/streams/news.ts").toString(), sink) { true }

            assertEquals(0L, written)
            assertEquals(0L, sink.length())
        }
}
