package com.johncorser.telly.features.player

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.video.VideoFrameMetadataListener
import androidx.test.core.app.ApplicationProvider
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Media3PlayerEngineTest {
    private val player = mockk<ExoPlayer>(relaxed = true)
    private val listener = slot<Player.Listener>()
    private val frameListener = slot<VideoFrameMetadataListener>()

    private fun engine(): Media3PlayerEngine {
        every { player.addListener(capture(listener)) } just Runs
        every { player.setVideoFrameMetadataListener(capture(frameListener)) } just Runs
        return Media3PlayerEngine(player)
    }

    @Test
    fun `load prepares the stream and reports buffering`() {
        val engine = engine()
        assertEquals(PlayerState.Idle, engine.state.value)

        engine.load("http://s/1.ts")

        assertEquals(PlayerState.Buffering, engine.state.value)
        verify {
            player.setMediaItem(any())
            player.prepare()
            player.play()
        }
    }

    @Test
    fun `load records the stream url so requests resolve its user-agent`() {
        every { player.addListener(capture(listener)) } just Runs
        every { player.setVideoFrameMetadataListener(capture(frameListener)) } just Runs
        val userAgent = StreamUserAgent(resolve = { "agent-for:$it" })
        val engine = Media3PlayerEngine(player, userAgent)

        engine.load("http://s/1.m3u8")

        assertEquals("agent-for:http://s/1.m3u8", userAgent.current())
    }

    @Test
    fun `ready playback exposes the stream's video details`() {
        val engine = engine()
        every { player.videoFormat } returns
            Format.Builder().setWidth(1280).setHeight(720).setFrameRate(25f).build()
        every { player.audioFormat } returns Format.Builder().setChannelCount(1).build()

        listener.captured.onPlaybackStateChanged(Player.STATE_BUFFERING)
        assertEquals(PlayerState.Buffering, engine.state.value)

        listener.captured.onPlaybackStateChanged(Player.STATE_READY)

        assertEquals(PlayerState.Playing, engine.state.value)
        assertEquals(VideoDetails(1280, 720, 25f, 1), engine.video.value)
    }

    @Test
    fun `ready without formats still plays with unknown details`() {
        val engine = engine()
        every { player.videoFormat } returns null
        every { player.audioFormat } returns null

        listener.captured.onPlaybackStateChanged(Player.STATE_READY)

        assertEquals(PlayerState.Playing, engine.state.value)
        assertEquals(VideoDetails(0, 0, 0f, 0), engine.video.value)
    }

    @Test
    fun `a TS stream without a container frame rate gets the measured one`() {
        val engine = engine()
        every { player.videoFormat } returns
            Format.Builder().setWidth(1280).setHeight(720).build()
        every { player.audioFormat } returns Format.Builder().setChannelCount(1).build()
        listener.captured.onPlaybackStateChanged(Player.STATE_READY)
        assertEquals(0f, engine.video.value!!.frameRate, 0f)

        val format = Format.Builder().build()
        for (frame in 0..12) {
            frameListener.captured.onVideoFrameAboutToBeRendered(frame * 40_000L, 0L, format, null)
        }

        assertEquals(25f, engine.video.value!!.frameRate, 0.01f)
    }

    @Test
    fun `a container-reported frame rate is never overridden`() {
        val engine = engine()
        every { player.videoFormat } returns
            Format.Builder().setWidth(1280).setHeight(720).setFrameRate(50f).build()
        every { player.audioFormat } returns Format.Builder().setChannelCount(1).build()
        listener.captured.onPlaybackStateChanged(Player.STATE_READY)

        val format = Format.Builder().build()
        for (frame in 0..12) {
            frameListener.captured.onVideoFrameAboutToBeRendered(frame * 40_000L, 0L, format, null)
        }

        assertEquals(50f, engine.video.value!!.frameRate, 0f)
    }

    @Test
    fun `errors surface their code name and idle states are ignored`() {
        val engine = engine()

        listener.captured.onPlaybackStateChanged(Player.STATE_IDLE)
        assertEquals(PlayerState.Idle, engine.state.value)

        listener.captured.onPlayerError(
            PlaybackException("boom", null, PlaybackException.ERROR_CODE_IO_UNSPECIFIED),
        )
        val state = engine.state.value
        assertEquals(PlayerState.Error("ERROR_CODE_IO_UNSPECIFIED"), state)
    }

    @Test
    fun `a finished stream reports Ended`() {
        val engine = engine()

        listener.captured.onPlaybackStateChanged(Player.STATE_ENDED)

        assertEquals(PlayerState.Ended, engine.state.value)
    }

    @Test
    fun `pause and resume drive the player and the paused signal`() {
        val engine = engine()
        assertEquals(false, engine.paused.value)

        engine.pause()
        assertEquals(true, engine.paused.value)

        engine.resume()
        assertEquals(false, engine.paused.value)

        verify {
            player.pause()
            player.play()
        }
    }

    @Test
    fun `load and stop reset the paused signal`() {
        val engine = engine()

        engine.pause()
        engine.load("http://s/1.ts")
        assertEquals(false, engine.paused.value)

        engine.pause()
        engine.stop()
        assertEquals(false, engine.paused.value)
    }

    @Test
    fun `stop and release pass through to the player`() {
        val engine = engine()
        engine.load("http://s/1.ts")

        engine.stop()
        assertEquals(PlayerState.Idle, engine.state.value)
        engine.release()

        verify {
            player.stop()
            player.release()
        }
    }

    @Test
    fun `create builds a real audio-focused ExoPlayer`() {
        val engine = Media3PlayerEngine.create(ApplicationProvider.getApplicationContext<Context>())

        assertNotNull(engine.player)
        assertEquals(PlayerState.Idle, engine.state.value)
        assertNull(engine.video.value)
        engine.release()
    }

    @Test
    fun `create wires the track facade with captions off by default`() {
        val engine = Media3PlayerEngine.create(ApplicationProvider.getApplicationContext<Context>())

        assertEquals(
            true,
            engine.player.trackSelectionParameters.disabledTrackTypes.contains(C.TRACK_TYPE_TEXT),
        )
        assertEquals(0L, engine.tracks.audioOffsetMs.value)
        engine.tracks.setAudioOffsetMs(100)
        assertEquals(100L, engine.tracks.audioOffsetMs.value)
        engine.release()
    }

    @Test
    fun `create without audio focus builds a player too (multiview panes)`() {
        val engine =
            Media3PlayerEngine.create(
                ApplicationProvider.getApplicationContext<Context>(),
                handleAudioFocus = false,
            )

        assertNotNull(engine.player)
        engine.release()
    }

    // Surround-by-default (and its precedence under an explicit user audio
    // pick) lives in ExoTrackFacade; see ExoTrackFacadeTest.

    @Test
    fun `create with passthrough forced builds a player too`() {
        val engine =
            Media3PlayerEngine.create(
                ApplicationProvider.getApplicationContext<Context>(),
                audio = PlayerAudioPrefs(passthrough = { true }),
            )

        assertNotNull(engine.player)
        engine.release()
    }

    @Test
    fun `mute drops the volume to zero and unmute restores it`() {
        val engine = engine()

        engine.setMuted(true)
        engine.setMuted(false)

        verify {
            player.volume = 0f
            player.volume = 1f
        }
    }
}
