package com.johncorser.telly.features.recording

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

/** OK in the capture player: pause/resume; at the file's end it replays. */
class RecordingPlayerToggleTest {
    private val player = mockk<ExoPlayer>(relaxed = true)

    @Test
    fun `OK toggles playWhenReady mid-stream`() {
        every { player.playbackState } returns Player.STATE_READY
        every { player.playWhenReady } returns true

        toggleRecordingPlayback(player)

        verify { player.playWhenReady = false }
    }

    @Test
    fun `OK at the capture's end replays from the start`() {
        every { player.playbackState } returns Player.STATE_ENDED

        toggleRecordingPlayback(player)

        verify {
            player.seekTo(0)
            player.playWhenReady = true
        }
    }
}
