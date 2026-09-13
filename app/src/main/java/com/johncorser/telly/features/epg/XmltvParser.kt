package com.johncorser.telly.features.epg

import org.xmlpull.v1.XmlPullParser
import java.io.Reader

/**
 * Streaming XMLTV parser. It walks the document once with an [XmlPullParser]
 * (injected so JVM tests can use kxml2 while devices use android.util.Xml),
 * is tolerant of missing fields, and skips programmes it cannot key or time.
 */
object XmltvParser {
    internal const val TAG_CHANNEL = "channel"
    internal const val TAG_PROGRAMME = "programme"

    /** Parses one XMLTV document from [reader] using [parser]. */
    fun parse(
        parser: XmlPullParser,
        reader: Reader,
    ): XmltvDocument {
        parser.setInput(reader)
        val channels = mutableListOf<XmltvChannel>()
        val programs = mutableListOf<XmltvProgram>()
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            when (parser.name) {
                TAG_CHANNEL -> XmltvElementReader.readChannel(parser)?.let { channels += it }
                TAG_PROGRAMME -> XmltvElementReader.readProgram(parser)?.let { programs += it }
            }
        }
        return XmltvDocument(channels = channels, programs = programs)
    }

    /**
     * Runs [onChild] at the start tag of each direct child of the current
     * element, consuming the whole element (unknown children are skipped).
     */
    internal fun forEachChildTag(
        parser: XmlPullParser,
        onChild: (String) -> Unit,
    ) {
        val elementDepth = parser.depth
        while (!(parser.next() == XmlPullParser.END_TAG && parser.depth == elementDepth)) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.depth == elementDepth + 1) {
                onChild(parser.name)
            }
        }
    }
}
