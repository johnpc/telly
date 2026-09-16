package com.johncorser.telly.features.recording

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class RoutingStreamRecorderTest {
    private val calls = mutableListOf<String>()

    private fun taggedRecorder(tag: String): StreamRecorder =
        StreamRecorder { url, _, _ ->
            calls += "$tag:$url"
            1L
        }

    private val routing = RoutingStreamRecorder(hls = taggedRecorder("hls"), progressive = taggedRecorder("ts"))

    @Test
    fun `m3u8 sources go to the HLS recorder, everything else to the byte copier`() =
        runTest {
            val sink = File.createTempFile("capture", ".ts")

            routing.copy("http://h/live/index.M3U8?token=1", sink) { false }
            routing.copy("http://h/streams/news.ts", sink) { false }
            routing.copy("http://h/live/42", sink) { false }

            assertEquals(
                listOf("hls:http://h/live/index.M3U8?token=1", "ts:http://h/streams/news.ts", "ts:http://h/live/42"),
                calls,
            )
        }
}
