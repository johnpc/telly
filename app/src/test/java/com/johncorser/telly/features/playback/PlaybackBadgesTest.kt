package com.johncorser.telly.features.playback

import com.johncorser.telly.features.player.VideoDetails
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackBadgesTest {
    @Test
    fun `derives the capture's HD 25fps mono badges`() {
        val badges = PlaybackBadges.badges(VideoDetails(1280, 720, 25f, 1))
        assertEquals(listOf("HD", "25 FPS", "MONO"), badges)
    }

    @Test
    fun `labels every resolution and audio tier`() {
        assertEquals(listOf("UHD", "STEREO"), PlaybackBadges.badges(VideoDetails(3840, 2160, 0f, 2)))
        assertEquals(listOf("FHD", "SURROUND"), PlaybackBadges.badges(VideoDetails(1920, 1080, 0f, 6)))
        assertEquals(listOf("SD"), PlaybackBadges.badges(VideoDetails(720, 576, 0f, 0)))
    }

    @Test
    fun `unknown stream details drop their badges`() {
        assertEquals(emptyList<String>(), PlaybackBadges.badges(null))
        assertEquals(emptyList<String>(), PlaybackBadges.badges(VideoDetails(0, 0, 0f, 0)))
        assertEquals(listOf("30 FPS"), PlaybackBadges.badges(VideoDetails(0, 0, 29.97f, 0)))
    }
}
