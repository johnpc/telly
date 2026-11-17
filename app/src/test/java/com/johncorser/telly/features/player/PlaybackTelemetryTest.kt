package com.johncorser.telly.features.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackTelemetryTest {
    private var now = 0L
    private val lines = mutableListOf<String>()
    private val telemetry = PlaybackTelemetry({ now }) { lines += it }

    @Test
    fun `state changes log the previous state's duration and repeats are ignored`() {
        now = 500
        telemetry.onState("BUFFERING", positionMs = 0)
        now = 2_000
        telemetry.onState("READY/playing", positionMs = 100)
        telemetry.onState("READY/playing", positionMs = 200)

        assertEquals(
            listOf(
                "state IDLE -> BUFFERING after 500ms at 0ms",
                "state BUFFERING -> READY/playing after 1500ms at 100ms",
            ),
            lines,
        )
    }

    @Test
    fun `video that stops rendering while audio advances is reported once, then its recovery`() {
        telemetry.onLoad("host/…/1.ts")
        telemetry.onVideoFrame()
        telemetry.onSample(playing = true, positionMs = 1_000, bufferedMs = 4_000)
        now = 2_500
        telemetry.onSample(playing = true, positionMs = 3_500, bufferedMs = 4_000)
        now = 4_500
        telemetry.onSample(playing = true, positionMs = 5_500, bufferedMs = 4_000)
        now = 6_000
        telemetry.onVideoFrame()

        assertEquals(
            listOf(
                "tune host/…/1.ts",
                "VIDEO FROZEN: no frame for 2500ms while audio advances, 4000ms buffered",
                "video resumed after 6000ms frozen",
            ),
            lines,
        )
    }

    @Test
    fun `no freeze is reported when paused, when the clock is stuck, or before the first frame`() {
        telemetry.onLoad("radio")
        now = 5_000
        telemetry.onSample(playing = true, positionMs = 5_000, bufferedMs = 0) // audio-only so far
        telemetry.onVideoFrame()
        now = 9_000
        telemetry.onSample(playing = false, positionMs = 6_000, bufferedMs = 0)
        telemetry.onSample(playing = true, positionMs = 6_000, bufferedMs = 0) // position stuck: StallDetector's job

        assertEquals(listOf("tune radio"), lines)
    }

    @Test
    fun `dropped-frame bursts, underruns, load errors and notes are logged`() {
        telemetry.onDroppedFrames(count = 2, elapsedMs = 1_000)
        telemetry.onDroppedFrames(count = 12, elapsedMs = 1_000)
        telemetry.onAudioUnderrun(bufferMs = 120)
        telemetry.onLoadError("HttpDataSourceException: timeout")
        telemetry.note("video decoder OMX.Nvidia.h264.decode")

        assertEquals(
            listOf(
                "dropped 12 frames in 1000ms",
                "audio underrun (120ms audio buffer)",
                "load error: HttpDataSourceException: timeout",
                "video decoder OMX.Nvidia.h264.decode",
            ),
            lines,
        )
    }

    @Test
    fun `stream labels keep the host and last segment, never credentials or tokens`() {
        assertEquals(
            "iptv.example:8080/…/12345.ts",
            streamLabel("http://user:pw@iptv.example:8080/live/user/pw/12345.ts?token=abc"),
        )
        assertEquals("cdn.example", streamLabel("https://cdn.example"))
        assertTrue(streamLabel("").isEmpty())
    }
}
