package com.johncorser.telly.features.onboarding

import com.johncorser.telly.features.playlist.M3uChannel
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaylistSummaryTest {
    private fun channel(
        url: String,
        group: String? = null,
    ) = M3uChannel(title = "c", streamUrl = url, groupTitle = group)

    @Test
    fun `mp4 and mkv stream urls count as movies, the rest as live channels`() {
        val channels =
            listOf(
                channel("http://s/one.ts"),
                channel("http://s/two.MP4"),
                channel("http://s/three.mkv?token=a"),
                channel("http://s/plain-path"),
            )

        assertEquals(2, PlaylistSummary.movieCount(channels))
        assertEquals(2, PlaylistSummary.liveCount(channels))
    }

    @Test
    fun `group count is distinct non-blank group titles`() {
        val channels =
            listOf(
                channel("http://s/1.ts", "News"),
                channel("http://s/2.ts", "News"),
                channel("http://s/3.ts", "Sports"),
                channel("http://s/4.ts", ""),
                channel("http://s/5.ts", null),
            )

        assertEquals(2, PlaylistSummary.groupCount(channels))
    }

    @Test
    fun `suggested playlist name is the url host`() {
        assertEquals("10.0.2.2", PlaylistSummary.suggestName("http://10.0.2.2:8090/playlist.m3u"))
        assertEquals("iptv.example.com", PlaylistSummary.suggestName("https://iptv.example.com/tv?u=a"))
    }

    @Test
    fun `unparseable urls fall back to the raw source string`() {
        assertEquals("not-a-url", PlaylistSummary.suggestName("not-a-url"))
    }
}
