package com.johncorser.telly.features.epg

import org.junit.Assert.assertEquals
import org.junit.Test
import org.tukaani.xz.LZMA2Options
import org.tukaani.xz.XZOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

class CompressedXmltvTest {
    private val xml = "<tv><programme>hi</programme></tv>"

    @Test
    fun `plain xml passes through unchanged`() {
        val reader = CompressedXmltv.reader(ByteArrayInputStream(xml.toByteArray()))

        assertEquals(xml, reader.readText())
    }

    @Test
    fun `gzip bodies are inflated`() {
        val reader = CompressedXmltv.reader(ByteArrayInputStream(gzip(xml)))

        assertEquals(xml, reader.readText())
    }

    @Test
    fun `xz bodies are inflated`() {
        val reader = CompressedXmltv.reader(ByteArrayInputStream(xz(xml)))

        assertEquals(xml, reader.readText())
    }

    @Test
    fun `a null body yields an empty reader`() {
        assertEquals("", CompressedXmltv.reader(null).readText())
    }

    private fun gzip(text: String): ByteArray =
        ByteArrayOutputStream().also { out -> GZIPOutputStream(out).use { it.write(text.toByteArray()) } }.toByteArray()

    private fun xz(text: String): ByteArray =
        ByteArrayOutputStream()
            .also { out -> XZOutputStream(out, LZMA2Options()).use { it.write(text.toByteArray()) } }
            .toByteArray()
}
