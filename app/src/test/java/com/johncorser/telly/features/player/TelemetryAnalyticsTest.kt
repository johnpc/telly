package com.johncorser.telly.features.player

import android.net.Uri
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.analytics.AnalyticsListener
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class TelemetryAnalyticsTest {
    private val lines = mutableListOf<String>()
    private val player = mockk<Player>(relaxed = true)
    private val telemetry = PlaybackTelemetry({ 0L }) { lines += it }
    private val analytics = TelemetryAnalytics(telemetry, player)
    private val codecs = TelemetryCodecAnalytics(telemetry)
    private val at = mockk<AnalyticsListener.EventTime>(relaxed = true)

    @Test
    fun `media3 callbacks land in the telemetry log`() {
        every { player.playbackState } returns Player.STATE_BUFFERING
        analytics.onMediaItemTransition(at, MediaItem.fromUri(Uri.parse("http://h.example/live/u/p/7.ts")), 0)
        analytics.onPlaybackStateChanged(at, Player.STATE_BUFFERING)
        every { player.playbackState } returns Player.STATE_READY
        every { player.isPlaying } returns true
        analytics.onIsPlayingChanged(at, true)
        analytics.onDroppedVideoFrames(at, 30, 1_000)
        analytics.onAudioUnderrun(at, 4096, 80, 10)
        analytics.onLoadError(at, mockk(relaxed = true), mockk(relaxed = true), IOException("reset"), false)
        codecs.onVideoDecoderInitialized(at, "c2.nvidia.avc.decoder", 0, 12)
        codecs.onAudioDecoderInitialized(at, "c2.android.aac.decoder", 0, 3)
        codecs.onVideoInputFormatChanged(
            at,
            Format.Builder().setSampleMimeType("video/avc").setWidth(1280).setHeight(720).build(),
            null,
        )
        analytics.onPlayerError(at, PlaybackException("boom", null, PlaybackException.ERROR_CODE_IO_UNSPECIFIED))

        assertEquals(
            listOf(
                "tune h.example/…/7.ts",
                "state IDLE -> BUFFERING after 0ms at 0ms",
                "state BUFFERING -> READY/playing after 0ms at 0ms",
                "dropped 30 frames in 1000ms",
                "audio underrun (80ms audio buffer)",
                "load error: IOException: reset",
                "video decoder c2.nvidia.avc.decoder (init 12ms)",
                "audio decoder c2.android.aac.decoder (init 3ms)",
                "video format video/avc 1280x720",
                "player error ERROR_CODE_IO_UNSPECIFIED: boom",
            ),
            lines,
        )
    }

    @Test
    fun `state labels distinguish playing from paused only when ready`() {
        assertEquals("READY/paused", stateLabel(Player.STATE_READY, isPlaying = false))
        assertEquals("ENDED", stateLabel(Player.STATE_ENDED, isPlaying = true))
        assertEquals("IDLE", stateLabel(Player.STATE_IDLE, isPlaying = false))
    }
}
