package com.johncorser.telly.features.playback

import com.johncorser.telly.features.player.VideoDetails
import org.junit.Assert.assertEquals
import org.junit.Test

class QuickBarTest {
    @Test
    fun `the nine captured slots render in order with live stream labels`() {
        val items = QuickBar.items(VideoDetails(1280, 720, 25f, 1))

        assertEquals(
            listOf(
                "Search", "Channels list", "Recordings", "Multiview",
                "Picture-in-picture", "1280 × 720", "Mono", "0 ms", "Off",
            ),
            items.map { it.label },
        )
        assertEquals(QuickBarAction.entries, items.map { it.action })
    }

    @Test
    fun `stereo and surround audio label accordingly`() {
        assertEquals("Stereo", QuickBar.items(VideoDetails(1920, 1080, 50f, 2))[6].label)
        assertEquals("Surround", QuickBar.items(VideoDetails(1920, 1080, 50f, 6))[6].label)
    }

    @Test
    fun `unknown stream details fall back to a dash`() {
        val items = QuickBar.items(null)
        assertEquals("—", items[5].label)
        assertEquals("—", items[6].label)

        val zero = QuickBar.items(VideoDetails(0, 0, 0f, 0))
        assertEquals("—", zero[5].label)
        assertEquals("—", zero[6].label)
    }

    @Test
    fun `every action names the feature its placeholder advertises`() {
        assertEquals("Video track", QuickBarAction.RESOLUTION.feature)
        assertEquals("Audio track", QuickBarAction.AUDIO.feature)
        assertEquals("Latency", QuickBarAction.LATENCY.feature)
        assertEquals("Subtitles", QuickBarAction.SUBTITLES.feature)
    }
}
