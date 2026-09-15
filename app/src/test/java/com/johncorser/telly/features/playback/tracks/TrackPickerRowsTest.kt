package com.johncorser.telly.features.playback.tracks

import com.johncorser.telly.features.player.tracks.AudioTrack
import com.johncorser.telly.features.player.tracks.TextTrack
import com.johncorser.telly.features.player.tracks.TrackSnapshot
import com.johncorser.telly.features.player.tracks.VideoTrack
import com.johncorser.telly.testutil.FakeTrackFacade
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackPickerRowsTest {
    private val snapshot =
        TrackSnapshot(
            videos = listOf(VideoTrack("0:0", 1920, 1080, 5_200_000), VideoTrack("0:1", 1280, 720, -1)),
            audios = listOf(AudioTrack("1:0", "en", 2), AudioTrack("1:1", null, 1)),
            texts = listOf(TextTrack("2:0", "en")),
            selectedAudioId = "1:0",
        )
    private val tracks = FakeTrackFacade(snapshot)

    @Test
    fun `the video picker lists Auto first and checks it while no override is set`() {
        val rows = TrackPickerRows.of(TrackPickerKind.VIDEO, snapshot)

        assertEquals(listOf("Auto", "1920×1080, 5.2 Mbps", "1280×720"), rows.map { it.label })
        assertEquals(listOf(true, false, false), rows.map { it.checked })
    }

    @Test
    fun `an active video override is the checked row instead of Auto`() {
        val rows = TrackPickerRows.of(TrackPickerKind.VIDEO, snapshot.copy(videoOverrideId = "0:1"))

        assertEquals(listOf(false, false, true), rows.map { it.checked })
    }

    @Test
    fun `the audio picker checks the playing track`() {
        val rows = TrackPickerRows.of(TrackPickerKind.AUDIO, snapshot)

        assertEquals(listOf("English · Stereo", "Audio 2 · Mono"), rows.map { it.label })
        assertEquals(listOf(true, false), rows.map { it.checked })
    }

    @Test
    fun `the CC picker lists Off first, checked until a track is enabled`() {
        assertEquals(
            listOf("Off" to true, "English" to false),
            TrackPickerRows.of(TrackPickerKind.SUBTITLES, snapshot).map { it.label to it.checked },
        )
        assertEquals(
            listOf(false, true),
            TrackPickerRows.of(TrackPickerKind.SUBTITLES, snapshot.copy(selectedTextId = "2:0")).map { it.checked },
        )
    }

    @Test
    fun `the sync picker is a stepper row set`() {
        val rows = TrackPickerRows.of(TrackPickerKind.SYNC, snapshot)

        assertEquals(listOf("-50 ms", "-25 ms", "+25 ms", "+50 ms", "Reset"), rows.map { it.label })
    }

    @Test
    fun `picking a video row applies the override and Auto clears it`() {
        assertTrue(TrackPickerRows.apply(TrackPickerKind.VIDEO, "0:1", tracks))
        assertTrue(TrackPickerRows.apply(TrackPickerKind.VIDEO, TrackPickerRows.AUTO, tracks))

        assertEquals(listOf("0:1", null), tracks.videoSelections)
    }

    @Test
    fun `picking audio and CC rows applies them and closes`() {
        assertTrue(TrackPickerRows.apply(TrackPickerKind.AUDIO, "1:1", tracks))
        assertTrue(TrackPickerRows.apply(TrackPickerKind.SUBTITLES, "2:0", tracks))
        assertTrue(TrackPickerRows.apply(TrackPickerKind.SUBTITLES, TrackPickerRows.OFF, tracks))

        assertEquals(listOf("1:1"), tracks.audioSelections)
        assertEquals(listOf("2:0", null), tracks.textSelections)
    }

    @Test
    fun `sync steps accumulate, clamp at the limit and keep the picker open`() {
        val rows = TrackPickerRows.of(TrackPickerKind.SYNC, snapshot)
        val plus50 = rows.first { it.label == "+50 ms" }.id
        val minus25 = rows.first { it.label == "-25 ms" }.id
        val reset = rows.first { it.label == "Reset" }.id

        assertFalse(TrackPickerRows.apply(TrackPickerKind.SYNC, plus50, tracks))
        assertFalse(TrackPickerRows.apply(TrackPickerKind.SYNC, minus25, tracks))
        assertEquals(25L, tracks.audioOffsetMs.value)

        repeat(25) { TrackPickerRows.apply(TrackPickerKind.SYNC, plus50, tracks) }
        assertEquals(TrackPickerRows.SYNC_LIMIT_MS, tracks.audioOffsetMs.value)

        assertFalse(TrackPickerRows.apply(TrackPickerKind.SYNC, reset, tracks))
        assertEquals(0L, tracks.audioOffsetMs.value)
    }
}
