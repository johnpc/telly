package com.johncorser.telly.features.recording

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HlsPlaylistParserTest {
    private val base = "http://host/live/stream.m3u8?token=abc"

    private fun media(text: String): HlsPlaylist.Media = HlsPlaylistParser.parse(text, base) as HlsPlaylist.Media

    @Test
    fun `a STREAM-INF line marks a master playlist and variants resolve`() {
        val playlist =
            HlsPlaylistParser.parse(
                """
                #EXTM3U
                #EXT-X-STREAM-INF:BANDWIDTH=800000,RESOLUTION=640x360,CODECS="avc1,mp4a"
                low/index.m3u8
                #EXT-X-STREAM-INF:BANDWIDTH=2400000,RESOLUTION=1280x720
                http://cdn/high/index.m3u8
                """.trimIndent(),
                base,
            )

        val master = playlist as HlsPlaylist.Master
        assertEquals(
            listOf(
                HlsVariant("http://host/live/low/index.m3u8", 800_000L),
                HlsVariant("http://cdn/high/index.m3u8", 2_400_000L),
            ),
            master.variants,
        )
        assertEquals("http://cdn/high/index.m3u8", master.best()?.url)
    }

    @Test
    fun `an empty master has no best variant and a missing BANDWIDTH reads as zero`() {
        assertNull(HlsPlaylist.Master(emptyList()).best())
        val master =
            HlsPlaylistParser.parse(
                "#EXTM3U\n#EXT-X-STREAM-INF:RESOLUTION=640x360\nonly.m3u8",
                base,
            ) as HlsPlaylist.Master
        assertEquals(0L, master.variants.single().bandwidth)
    }

    @Test
    fun `a media playlist lists segments with sequence numbers from MEDIA-SEQUENCE`() {
        val playlist =
            media(
                """
                #EXTM3U
                #EXT-X-VERSION:3
                #EXT-X-TARGETDURATION:4
                #EXT-X-MEDIA-SEQUENCE:17
                #EXTINF:4.0,
                seg17.ts
                #EXTINF:4.0,
                seg18.ts
                #EXTINF:2.5,
                /root/seg19.ts
                """.trimIndent(),
            )

        assertEquals(
            listOf(
                HlsSegment(17L, "http://host/live/seg17.ts"),
                HlsSegment(18L, "http://host/live/seg18.ts"),
                HlsSegment(19L, "http://host/root/seg19.ts"),
            ),
            playlist.segments,
        )
        assertEquals(4_000L, playlist.targetDurationMs)
        assertFalse(playlist.ended)
        assertNull(playlist.initSegmentUrl)
    }

    @Test
    fun `MEDIA-SEQUENCE defaults to zero and blank lines are skipped`() {
        val playlist = media("#EXTM3U\n\n#EXTINF:2.0,\na.ts\n\n#EXTINF:2.0,\nb.ts\n")
        assertEquals(listOf(0L, 1L), playlist.segments.map { it.sequence })
    }

    @Test
    fun `ENDLIST marks a finished playlist`() {
        val playlist = media("#EXTM3U\n#EXTINF:2.0,\na.ts\n#EXT-X-ENDLIST")
        assertTrue(playlist.ended)
    }

    @Test
    fun `EXT-X-MAP detects fMP4 and resolves the init segment URI`() {
        val playlist =
            media(
                """
                #EXTM3U
                #EXT-X-TARGETDURATION:6
                #EXT-X-MAP:URI="init.mp4",BYTERANGE="720@0"
                #EXTINF:6.0,
                seg0.m4s
                """.trimIndent(),
            )
        assertEquals("http://host/live/init.mp4", playlist.initSegmentUrl)
        assertEquals("http://host/live/seg0.m4s", playlist.segments.single().url)
    }

    @Test
    fun `garbled TARGETDURATION and MEDIA-SEQUENCE fall back to defaults`() {
        val playlist = media("#EXT-X-TARGETDURATION:soon\n#EXT-X-MEDIA-SEQUENCE:many\na.ts")
        assertEquals(HlsPlaylistParser.DEFAULT_TARGET_DURATION_MS, playlist.targetDurationMs)
        assertEquals(0L, playlist.segments.single().sequence)
        assertEquals(HlsPlaylistParser.DEFAULT_TARGET_DURATION_MS, media("a.ts").targetDurationMs)
    }

    @Test
    fun `resolve keeps absolute references and survives unparseable bases`() {
        assertEquals("http://cdn/a.ts", HlsPlaylistParser.resolve(base, "http://cdn/a.ts"))
        assertEquals("a.ts", HlsPlaylistParser.resolve("ht tp://broken base", "a.ts"))
        assertEquals("http://host/live/a.ts", HlsPlaylistParser.resolve(base, "a.ts"))
    }
}
