package com.johncorser.telly.features.playback.tracks

import com.johncorser.telly.features.player.tracks.AudioTrack
import com.johncorser.telly.features.player.tracks.TextTrack
import com.johncorser.telly.features.player.tracks.TrackSnapshot
import com.johncorser.telly.features.player.tracks.VideoTrack
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackLabelsTest {
    @Test
    fun `video labels show resolution and bitrate when known`() {
        assertEquals("1920×1080, 5.2 Mbps", TrackLabels.video(VideoTrack("0:0", 1920, 1080, 5_200_000)))
        assertEquals("1280×720", TrackLabels.video(VideoTrack("0:0", 1280, 720, -1)))
        assertEquals("Video", TrackLabels.video(VideoTrack("0:0", -1, -1, -1)))
    }

    @Test
    fun `audio labels show language and channel layout`() {
        assertEquals("English · Stereo", TrackLabels.audio(AudioTrack("1:0", "en", 2), 0))
        assertEquals("Audio 1 · Mono", TrackLabels.audio(AudioTrack("1:0", null, 1), 0))
        assertEquals("Audio 2 · Surround", TrackLabels.audio(AudioTrack("1:1", "und", 6), 1))
        assertEquals("Audio 1", TrackLabels.audio(AudioTrack("1:0", "", 0), 0))
    }

    @Test
    fun `text labels show the language or a numbered fallback`() {
        assertEquals("English", TrackLabels.text(TextTrack("2:0", "en"), 0))
        assertEquals("Subtitles 2", TrackLabels.text(TextTrack("2:1", null), 1))
    }

    @Test
    fun `sync labels carry an explicit sign for positive offsets`() {
        assertEquals("0 ms", TrackLabels.sync(0))
        assertEquals("+150 ms", TrackLabels.sync(150))
        assertEquals("-75 ms", TrackLabels.sync(-75))
    }

    @Test
    fun `the CC slot label flips from Off to the enabled track`() {
        val texts = listOf(TextTrack("2:0", "en"))
        assertEquals("Off", TrackLabels.subtitleSlot(TrackSnapshot(texts = texts)))
        assertEquals("English", TrackLabels.subtitleSlot(TrackSnapshot(texts = texts, selectedTextId = "2:0")))
        assertEquals("Off", TrackLabels.subtitleSlot(TrackSnapshot(texts = texts, selectedTextId = "9:9")))
    }
}
