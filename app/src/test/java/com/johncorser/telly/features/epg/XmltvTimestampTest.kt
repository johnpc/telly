package com.johncorser.telly.features.epg

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class XmltvTimestampTest {
    private val noonUtc = 1_789_302_600_000L // 2026-09-13T12:30:00Z

    @Test
    fun `parses a utc timestamp with an explicit +0000 offset`() {
        assertEquals(noonUtc, XmltvTimestamp.parseMs("20260913123000 +0000"))
    }

    @Test
    fun `parses a timestamp without any offset as utc`() {
        assertEquals(noonUtc, XmltvTimestamp.parseMs("20260913123000"))
    }

    @Test
    fun `applies positive offsets by shifting the instant back`() {
        // 14:30 at +0200 is 12:30 UTC.
        assertEquals(noonUtc, XmltvTimestamp.parseMs("20260913143000 +0200"))
    }

    @Test
    fun `applies negative offsets by shifting the instant forward`() {
        // 07:00 at -0530 is 12:30 UTC.
        assertEquals(noonUtc, XmltvTimestamp.parseMs("20260913070000 -0530"))
    }

    @Test
    fun `tolerates surrounding whitespace`() {
        assertEquals(noonUtc, XmltvTimestamp.parseMs("  20260913123000 +0000  "))
    }

    @Test
    fun `parses the unix epoch`() {
        assertEquals(0L, XmltvTimestamp.parseMs("19700101000000 +0000"))
    }

    @Test
    fun `rejects null blank truncated and garbage inputs`() {
        assertNull(XmltvTimestamp.parseMs(null))
        assertNull(XmltvTimestamp.parseMs(""))
        assertNull(XmltvTimestamp.parseMs("   "))
        assertNull(XmltvTimestamp.parseMs("2026091312"))
        assertNull(XmltvTimestamp.parseMs("not-a-timestamp"))
        assertNull(XmltvTimestamp.parseMs("20260913123000 UTC"))
    }
}
