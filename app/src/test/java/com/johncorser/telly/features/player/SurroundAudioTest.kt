package com.johncorser.telly.features.player

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.TrackGroup
import androidx.media3.common.Tracks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SurroundAudioTest {
    private fun audioFormat(channels: Int): Format =
        Format
            .Builder()
            .setSampleMimeType(MimeTypes.AUDIO_AAC)
            .setChannelCount(channels)
            .build()

    private fun group(
        selected: Int?,
        supported: Boolean = true,
        vararg channels: Int,
    ): Tracks.Group =
        Tracks.Group(
            TrackGroup(*channels.map(::audioFormat).toTypedArray()),
            false,
            IntArray(channels.size) { if (supported) C.FORMAT_HANDLED else C.FORMAT_UNSUPPORTED_TYPE },
            BooleanArray(channels.size) { it == selected },
        )

    @Test
    fun `picks the supported track with the most channels over a stereo selection`() {
        val tracks = Tracks(listOf(group(0, true, 2, 6)))

        val pick = SurroundAudio.pick(tracks)!!

        assertEquals(1, pick.trackIndices.single())
        assertEquals(6, pick.mediaTrackGroup.getFormat(1).channelCount)
    }

    @Test
    fun `no override when the surround track is already selected`() {
        assertNull(SurroundAudio.pick(Tracks(listOf(group(1, true, 2, 6)))))
    }

    @Test
    fun `no override when nothing beats the selected channel count`() {
        assertNull(SurroundAudio.pick(Tracks(listOf(group(0, true, 2, 2)))))
    }

    @Test
    fun `unsupported tracks never win`() {
        assertNull(SurroundAudio.pick(Tracks(listOf(group(0, false, 2, 6)))))
    }

    @Test
    fun `no audio groups means no override`() {
        assertNull(SurroundAudio.pick(Tracks.EMPTY))
    }

    @Test
    fun `the best track can come from another group`() {
        val stereo = group(0, true, 2)
        val surround = group(null, true, 6, 8)

        val pick = SurroundAudio.pick(Tracks(listOf(stereo, surround)))!!

        assertEquals(8, pick.mediaTrackGroup.getFormat(pick.trackIndices.single()).channelCount)
    }
}
