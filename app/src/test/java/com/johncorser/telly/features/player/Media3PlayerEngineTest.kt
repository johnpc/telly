package com.johncorser.telly.features.player

import android.content.Context
import androidx.media3.common.Format
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
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

    private fun engine(): Media3PlayerEngine {
        every { player.addListener(capture(listener)) } just Runs
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
    fun `errors surface their code name and other states are ignored`() {
        val engine = engine()

        listener.captured.onPlaybackStateChanged(Player.STATE_ENDED)
        assertEquals(PlayerState.Idle, engine.state.value)

        listener.captured.onPlayerError(
            PlaybackException("boom", null, PlaybackException.ERROR_CODE_IO_UNSPECIFIED),
        )
        val state = engine.state.value
        assertEquals(PlayerState.Error("ERROR_CODE_IO_UNSPECIFIED"), state)
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
}
