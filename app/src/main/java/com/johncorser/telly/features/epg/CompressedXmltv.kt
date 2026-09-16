package com.johncorser.telly.features.epg

import org.tukaani.xz.XZInputStream
import java.io.BufferedInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringReader
import java.util.zip.GZIPInputStream

/**
 * Wraps an EPG response body in a decompressing [Reader]. Many providers hand
 * out `guide.xml.gz` / `guide.xml.xz` and OkHttp does NOT transparently inflate
 * `application/gzip` (only `Content-Encoding: gzip`), so we sniff the leading
 * magic bytes ourselves and pick the right stream — falling back to plain UTF-8
 * for uncompressed XMLTV. A null body yields an empty reader (parses to zero
 * programmes), mirroring the old behaviour.
 */
object CompressedXmltv {
    private const val BYTE_MASK = 0xFF
    private val GZIP = byteArrayOf(0x1f, 0x8b.toByte())
    private val XZ = byteArrayOf(0xFD.toByte(), 0x37, 0x7A, 0x58, 0x5A, 0x00)

    fun reader(input: InputStream?): Reader {
        input ?: return StringReader("")
        val buffered = BufferedInputStream(input)
        return InputStreamReader(decompress(buffered), Charsets.UTF_8)
    }

    private fun decompress(input: BufferedInputStream): InputStream =
        when {
            startsWith(input, GZIP) -> GZIPInputStream(input)
            startsWith(input, XZ) -> XZInputStream(input)
            else -> input
        }

    /** Peeks [magic].size bytes without consuming them, so the stream still parses. */
    private fun startsWith(
        input: BufferedInputStream,
        magic: ByteArray,
    ): Boolean {
        input.mark(magic.size)
        val matched = magic.all { input.read() == (it.toInt() and BYTE_MASK) }
        input.reset()
        return matched
    }
}
