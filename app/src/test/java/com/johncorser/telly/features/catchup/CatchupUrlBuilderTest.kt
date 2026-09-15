package com.johncorser.telly.features.catchup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CatchupUrlBuilderTest {
    // 2026-09-13 12:00:00 UTC — values chosen so every placeholder differs.
    private val startMs = 1_789_300_800_000L
    private val endMs = startMs + 90 * 60_000L
    private val nowMs = endMs + 30 * 60_000L
    private val startSec = startMs / 1_000
    private val nowSec = nowMs / 1_000

    private fun build(
        type: CatchupType,
        source: String?,
        streamUrl: String = "http://host:8080/live/stream.m3u8",
    ): String? = CatchupUrlBuilder.build(streamUrl, CatchupAttributes(type, source, days = 7), startMs, endMs, nowMs)

    @Test
    fun `default substitutes every placeholder in epoch seconds`() {
        val url =
            build(
                CatchupType.DEFAULT,
                "http://a/c?u={utc}&s={start}&l={lutc}&n={now}&t={timestamp}&o={offset}&d={duration}",
            )

        assertEquals(
            "http://a/c?u=$startSec&s=$startSec&l=$nowSec&n=$nowSec&t=$nowSec&o=${nowSec - startSec}&d=${90 * 60}",
            url,
        )
    }

    @Test
    fun `dollar-brace placeholder variants substitute too`() {
        assertEquals(
            "http://a/c?u=$startSec&d=${90 * 60}",
            build(CatchupType.DEFAULT, "http://a/c?u=\${start}&d=\${duration}"),
        )
    }

    @Test
    fun `default without a template yields nothing`() {
        assertNull(build(CatchupType.DEFAULT, source = null))
    }

    @Test
    fun `append concatenates the expanded template onto the live URL`() {
        assertEquals(
            "http://host:8080/live/stream.m3u8?utc=$startSec&lutc=$nowSec",
            build(CatchupType.APPEND, "?utc={utc}&lutc={lutc}"),
        )
    }

    @Test
    fun `shift appends utc and lutc query params to the live URL`() {
        assertEquals(
            "http://host:8080/live/stream.m3u8?utc=$startSec&lutc=$nowSec",
            build(CatchupType.SHIFT, source = null),
        )
    }

    @Test
    fun `shift respects an existing query string`() {
        assertEquals(
            "http://h/s.ts?token=x&utc=$startSec&lutc=$nowSec",
            build(CatchupType.SHIFT, source = null, streamUrl = "http://h/s.ts?token=x"),
        )
    }

    @Test
    fun `flussonic rewrites the playlist segment to the archive form`() {
        assertEquals(
            "http://h/ch/archive-$startSec-${90 * 60}.m3u8",
            build(CatchupType.FLUSSONIC, source = null, streamUrl = "http://h/ch/mono.m3u8"),
        )
    }

    @Test
    fun `flussonic keeps video-prefixed playlists and the query string`() {
        assertEquals(
            "http://h/ch/video-$startSec-${90 * 60}.m3u8?token=x",
            build(CatchupType.FLUSSONIC, source = null, streamUrl = "http://h/ch/video.m3u8?token=x"),
        )
    }

    @Test
    fun `flussonic rewrites mpegts streams to the ts archive`() {
        assertEquals(
            "http://h/ch/archive-$startSec-${90 * 60}.ts",
            build(CatchupType.FLUSSONIC, source = null, streamUrl = "http://h/ch/mpegts"),
        )
    }

    @Test
    fun `xc rewrites the conventional live URL to the timeshift endpoint`() {
        assertEquals(
            "http://h:8080/timeshift/user/pass/90/2026-09-13:12-00/42.ts",
            build(CatchupType.XC, source = null, streamUrl = "http://h:8080/live/user/pass/42.ts"),
        )
    }

    @Test
    fun `xc accepts live URLs without the live prefix or extension`() {
        assertEquals(
            "https://h/timeshift/u/p/90/2026-09-13:12-00/7.ts",
            build(CatchupType.XC, source = null, streamUrl = "https://h/u/p/7"),
        )
    }

    @Test
    fun `xc rounds partial minutes up and rejects unconventional URLs`() {
        val url =
            CatchupUrlBuilder.build(
                "http://h/live/u/p/7.m3u8",
                CatchupAttributes(CatchupType.XC, null, days = 1),
                startMs,
                startMs + 61_000L,
                nowMs,
            )

        assertEquals("http://h/timeshift/u/p/2/2026-09-13:12-00/7.ts", url)
        assertNull(build(CatchupType.XC, source = null, streamUrl = "http://h/only-one-segment"))
    }
}
