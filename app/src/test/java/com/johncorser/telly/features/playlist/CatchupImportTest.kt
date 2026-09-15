package com.johncorser.telly.features.playlist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The M3U catch-up attributes: parser capture + importer persistence. */
class CatchupImportTest {
    private val playlist =
        """
        #EXTM3U
        #EXTINF:-1 tvg-id="one" catchup="default" catchup-source="http://a/arc?utc={utc}&d={duration}" catchup-days="3",One
        http://a/live.ts
        #EXTINF:-1 tvg-id="two" catchup-type="shift",Two
        http://a/two.ts
        #EXTINF:-1 tvg-id="three",Three
        http://a/three.ts
        """.trimIndent()

    @Test
    fun `the parser captures catchup, catchup-source and catchup-days`() {
        val channel = M3uParser.parse(playlist).channels.first()

        assertEquals("default", channel.catchup)
        assertEquals("http://a/arc?utc={utc}&d={duration}", channel.catchupSource)
        assertEquals(3, channel.catchupDays)
    }

    @Test
    fun `the older catchup-type spelling is accepted`() {
        assertEquals("shift", M3uParser.parse(playlist).channels[1].catchup)
    }

    @Test
    fun `channels without the attributes parse as plain live channels`() {
        val channel = M3uParser.parse(playlist).channels[2]

        assertNull(channel.catchup)
        assertNull(channel.catchupSource)
        assertNull(channel.catchupDays)
    }

    @Test
    fun `the importer carries the attributes onto the channel rows`() {
        val rows =
            ChannelImporter.import(
                playlistId = 1,
                parsed = M3uParser.parse(playlist).channels,
                previous = emptyList(),
            )

        assertEquals("default", rows[0].catchup.catchupType)
        assertEquals("http://a/arc?utc={utc}&d={duration}", rows[0].catchup.catchupSource)
        assertEquals(3, rows[0].catchup.catchupDays)
        assertEquals("shift", rows[1].catchup.catchupType)
        assertNull(rows[2].catchup.catchupType)
    }

    @Test
    fun `a refresh re-imports the attributes from the fresh parse`() {
        val first = ChannelImporter.import(1, M3uParser.parse(playlist).channels, previous = emptyList())
        val updated = playlist.replace("catchup-days=\"3\"", "catchup-days=\"9\"")

        val second = ChannelImporter.import(1, M3uParser.parse(updated).channels, previous = first)

        assertEquals(9, second[0].catchup.catchupDays)
    }
}
