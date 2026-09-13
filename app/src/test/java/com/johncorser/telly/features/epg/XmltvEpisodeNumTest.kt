package com.johncorser.telly.features.epg

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class XmltvEpisodeNumTest {
    @Test
    fun `xmltv_ns season and episode are zero-based`() {
        assertEquals("S1 E10", XmltvEpisodeNum.display("xmltv_ns", "0.9."))
        assertEquals("S2 E1", XmltvEpisodeNum.display("xmltv_ns", "1.0.0"))
    }

    @Test
    fun `xmltv_ns tolerates whitespace and index-of-total fields`() {
        assertEquals("S1 E10", XmltvEpisodeNum.display("xmltv_ns", " 0 . 9 . "))
        assertEquals("S1 E10", XmltvEpisodeNum.display(" xmltv_ns ", "0/2.9/20.0/2"))
    }

    @Test
    fun `xmltv_ns missing fields drop their part`() {
        assertEquals("E10", XmltvEpisodeNum.display("xmltv_ns", ".9."))
        assertEquals("S1", XmltvEpisodeNum.display("xmltv_ns", "0.."))
        assertEquals("S1", XmltvEpisodeNum.display("xmltv_ns", "0"))
    }

    @Test
    fun `unparseable xmltv_ns renders nothing`() {
        assertNull(XmltvEpisodeNum.display("xmltv_ns", ".."))
        assertNull(XmltvEpisodeNum.display("xmltv_ns", "abc"))
        assertNull(XmltvEpisodeNum.display("xmltv_ns", "-1.-2."))
    }

    @Test
    fun `other systems pass their display text through`() {
        assertEquals("S2 E5", XmltvEpisodeNum.display("onscreen", "S2 E5"))
        assertEquals("212", XmltvEpisodeNum.display(null, " 212 "))
    }

    @Test
    fun `blank text renders nothing for every system`() {
        assertNull(XmltvEpisodeNum.display("xmltv_ns", null))
        assertNull(XmltvEpisodeNum.display("onscreen", "  "))
        assertNull(XmltvEpisodeNum.display(null, ""))
    }
}
