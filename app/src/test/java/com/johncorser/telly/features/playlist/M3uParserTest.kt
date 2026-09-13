package com.johncorser.telly.features.playlist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class M3uParserTest {
    private val extInfLine =
        "#EXTINF:-1 tvg-id=\"bbc1.uk\" tvg-logo=\"https://logo.example/bbc1.png\" group-title=\"News\",BBC One"

    @Test
    fun `recognises the playlist header`() {
        assertTrue(M3uParser.isHeader("#EXTM3U"))
        assertTrue(M3uParser.isHeader("  #EXTM3U url-tvg=\"https://epg.example/guide.xml\"  "))
        assertFalse(M3uParser.isHeader("#EXTINF:-1,BBC One"))
    }

    @Test
    fun `recognises extinf lines`() {
        assertTrue(M3uParser.isExtInf(extInfLine))
        assertFalse(M3uParser.isExtInf("https://stream.example/bbc1.m3u8"))
    }

    @Test
    fun `parses title and attributes from an extinf line`() {
        val entry = M3uParser.parseExtInf(extInfLine)

        val expected =
            M3uEntry(
                title = "BBC One",
                attributes =
                    mapOf(
                        "tvg-id" to "bbc1.uk",
                        "tvg-logo" to "https://logo.example/bbc1.png",
                        "group-title" to "News",
                    ),
            )
        assertEquals(expected, entry)
        assertEquals(expected.hashCode(), entry.hashCode())
    }

    @Test
    fun `parses an extinf line without attributes`() {
        val entry = M3uParser.parseExtInf("#EXTINF:-1,Channel 4")

        assertEquals(M3uEntry(title = "Channel 4", attributes = emptyMap()), entry)
    }

    @Test
    fun `returns null for lines that are not extinf`() {
        assertNull(M3uParser.parseExtInf("#EXTM3U"))
        assertNull(M3uParser.parseExtInf("https://stream.example/bbc1.m3u8"))
    }

    @Test
    fun `returns null when the extinf line has no title`() {
        assertNull(M3uParser.parseExtInf("#EXTINF:-1 tvg-id=\"bbc1.uk\""))
        assertNull(M3uParser.parseExtInf("#EXTINF"))
    }

    @Test
    fun `entries behave as value objects`() {
        val entry = M3uEntry(title = "BBC One", attributes = mapOf("tvg-id" to "bbc1.uk"))

        val renamed = entry.copy(title = "BBC Two")
        assertNotEquals(entry, renamed)
        assertEquals("BBC One", entry.component1())
        assertEquals(mapOf("tvg-id" to "bbc1.uk"), entry.component2())
        assertTrue(entry.toString().contains("BBC One"))
    }
}
