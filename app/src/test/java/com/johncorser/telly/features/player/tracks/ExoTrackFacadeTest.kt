package com.johncorser.telly.features.player.tracks

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.test.core.app.ApplicationProvider
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExoTrackFacadeTest {
    private val player = mockk<Player>(relaxed = true)
    private val listener = slot<Player.Listener>()
    private val holder = AudioOffsetHolder()

    private fun facade(): ExoTrackFacade {
        every { player.addListener(capture(listener)) } just Runs
        every { player.trackSelectionParameters } returns
            TrackSelectionParameters.getDefaults(ApplicationProvider.getApplicationContext())
        return ExoTrackFacade(player, holder)
    }

    private fun group(
        format: Format,
        selected: Boolean = false,
    ): Tracks.Group = Tracks.Group(TrackGroup(format), false, intArrayOf(C.FORMAT_HANDLED), booleanArrayOf(selected))

    private fun videoFormat(width: Int = 1920) =
        Format
            .Builder()
            .setSampleMimeType(MimeTypes.VIDEO_H264)
            .setWidth(width)
            .setHeight(width * 9 / 16)
            .setAverageBitrate(5_200_000)
            .build()

    private fun audioFormat() =
        Format
            .Builder()
            .setSampleMimeType(MimeTypes.AUDIO_AAC)
            .setLanguage("en")
            .setChannelCount(2)
            .build()

    private fun textFormat() =
        Format
            .Builder()
            .setSampleMimeType(MimeTypes.TEXT_VTT)
            .setLanguage("en")
            .build()

    private fun allTracks() =
        Tracks(listOf(group(videoFormat()), group(audioFormat(), selected = true), group(textFormat())))

    @Test
    fun `tracks changes map into a snapshot with the playing selections`() {
        val facade = facade()

        listener.captured.onTracksChanged(allTracks())

        val snapshot = facade.snapshot.value
        assertEquals(listOf(VideoTrack("0:0", 1920, 1080, 5_200_000)), snapshot.videos)
        assertEquals(listOf(AudioTrack("1:0", "en", 2)), snapshot.audios)
        assertEquals(listOf(TextTrack("2:0", "en")), snapshot.texts)
        assertNull(snapshot.videoOverrideId)
        assertEquals("1:0", snapshot.selectedAudioId)
        assertNull(snapshot.selectedTextId)
    }

    @Test
    fun `selecting a video track applies an override and Auto clears it`() {
        val facade = facade()
        listener.captured.onTracksChanged(allTracks())
        val applied = mutableListOf<TrackSelectionParameters>()

        facade.selectVideo("0:0")
        verify { player.trackSelectionParameters = capture(applied) }
        assertEquals(1, applied.last().overrides.size)
        assertEquals("0:0", facade.snapshot.value.videoOverrideId)

        facade.selectVideo(null)
        verify { player.trackSelectionParameters = capture(applied) }
        assertTrue(applied.last().overrides.isEmpty())
        assertNull(facade.snapshot.value.videoOverrideId)
    }

    @Test
    fun `selecting audio applies its override and updates the snapshot`() {
        val facade = facade()
        listener.captured.onTracksChanged(allTracks())
        val applied = slot<TrackSelectionParameters>()

        facade.selectAudio("1:0")

        verify { player.trackSelectionParameters = capture(applied) }
        assertEquals(1, applied.captured.overrides.size)
        assertEquals("1:0", facade.snapshot.value.selectedAudioId)
    }

    @Test
    fun `enabling a text track lifts the disabled flag and Off restores it`() {
        val facade = facade()
        listener.captured.onTracksChanged(allTracks())
        val applied = mutableListOf<TrackSelectionParameters>()

        facade.selectText("2:0")
        verify { player.trackSelectionParameters = capture(applied) }
        assertFalse(applied.last().disabledTrackTypes.contains(C.TRACK_TYPE_TEXT))
        assertEquals(1, applied.last().overrides.size)
        assertEquals("2:0", facade.snapshot.value.selectedTextId)

        facade.selectText(null)
        verify { player.trackSelectionParameters = capture(applied) }
        assertTrue(applied.last().disabledTrackTypes.contains(C.TRACK_TYPE_TEXT))
        assertNull(facade.snapshot.value.selectedTextId)
    }

    @Test
    fun `a zap to a different stream drops the stale video override`() {
        val facade = facade()
        listener.captured.onTracksChanged(allTracks())
        facade.selectVideo("0:0")

        // The same stream re-reporting keeps the pick "checked"…
        listener.captured.onTracksChanged(allTracks())
        assertEquals("0:0", facade.snapshot.value.videoOverrideId)

        // …but a new stream whose "0:0" is a different group resets to Auto.
        listener.captured.onTracksChanged(Tracks(listOf(group(videoFormat(width = 1280)))))
        assertNull(facade.snapshot.value.videoOverrideId)
    }

    @Test
    fun `the audio offset lands in the shared holder and the flow`() {
        val facade = facade()

        facade.setAudioOffsetMs(150)

        assertEquals(150L, holder.offsetMs)
        assertEquals(150L, facade.audioOffsetMs.value)
    }
}
