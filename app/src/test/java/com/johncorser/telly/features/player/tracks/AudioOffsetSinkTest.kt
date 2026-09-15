package com.johncorser.telly.features.player.tracks

import androidx.media3.exoplayer.audio.AudioSink
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AudioOffsetSinkTest {
    private val delegate = mockk<AudioSink>(relaxed = true)
    private val holder = AudioOffsetHolder()
    private val sink = AudioOffsetSink(delegate, holder)

    @Test
    fun `a positive offset advances the reported audio clock`() {
        every { delegate.getCurrentPositionUs(false) } returns 1_000_000L
        holder.offsetMs = 150

        assertEquals(1_150_000L, sink.getCurrentPositionUs(false))
    }

    @Test
    fun `a negative offset pulls the reported audio clock back`() {
        every { delegate.getCurrentPositionUs(false) } returns 1_000_000L
        holder.offsetMs = -75

        assertEquals(925_000L, sink.getCurrentPositionUs(false))
    }

    @Test
    fun `a zero offset passes the position through untouched`() {
        every { delegate.getCurrentPositionUs(true) } returns 42L

        assertEquals(42L, sink.getCurrentPositionUs(true))
    }

    @Test
    fun `an unset position is never shifted`() {
        every { delegate.getCurrentPositionUs(false) } returns AudioSink.CURRENT_POSITION_NOT_SET
        holder.offsetMs = 500

        assertEquals(AudioSink.CURRENT_POSITION_NOT_SET, sink.getCurrentPositionUs(false))
    }
}
