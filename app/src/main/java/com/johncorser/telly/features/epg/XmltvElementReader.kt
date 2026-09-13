package com.johncorser.telly.features.epg

import com.johncorser.telly.features.epg.db.ProgramDetails
import org.xmlpull.v1.XmlPullParser

/** Reads single `<channel>` / `<programme>` elements for [XmltvParser]. */
internal object XmltvElementReader {
    private val channelTextTags = setOf("display-name")
    private val programTextTags = setOf("title", "desc", "category", "episode-num")

    /** Returns the channel, or null when the mandatory `id` is missing. */
    fun readChannel(parser: XmlPullParser): XmltvChannel? {
        val id = parser.getAttributeValue(null, "id")
        val children = collectChildren(parser, channelTextTags)
        if (id.isNullOrBlank()) return null
        return XmltvChannel(id = id, displayName = children["display-name"], iconUrl = children["icon"])
    }

    /** Returns the programme, or null when channel/start/stop/title are unusable. */
    fun readProgram(parser: XmlPullParser): XmltvProgram? {
        val channelId = parser.getAttributeValue(null, "channel")
        val startMs = XmltvTimestamp.parseMs(parser.getAttributeValue(null, "start"))
        val endMs = XmltvTimestamp.parseMs(parser.getAttributeValue(null, "stop"))
        val children = collectChildren(parser, programTextTags)
        val details =
            children["title"]?.takeIf { it.isNotBlank() }?.let { title ->
                ProgramDetails(
                    title = title,
                    description = children["desc"],
                    category = children["category"],
                    episode = children["episode-num"],
                )
            }
        return buildProgram(channelId, startMs, endMs, details)
    }

    /**
     * Consumes the current element, keeping the first occurrence of each
     * interesting child: element text for [textTags], `src` for `<icon>`.
     * Unknown children (and their subtrees) are skipped, tolerantly.
     */
    private fun collectChildren(
        parser: XmlPullParser,
        textTags: Set<String>,
    ): Map<String, String> {
        val found = mutableMapOf<String, String>()
        XmltvParser.forEachChildTag(parser) { tag ->
            val value = childValue(parser, tag, textTags)
            if (value != null && tag !in found) found[tag] = value
        }
        return found
    }

    /** The value one child contributes, or null when it is not interesting. */
    private fun childValue(
        parser: XmlPullParser,
        tag: String,
        textTags: Set<String>,
    ): String? =
        when {
            tag == "icon" -> parser.getAttributeValue(null, "src")
            tag in textTags -> parser.nextText()
            else -> null
        }

    /** Assembles a programme once every mandatory piece is present. */
    private fun buildProgram(
        channelId: String?,
        startMs: Long?,
        endMs: Long?,
        details: ProgramDetails?,
    ): XmltvProgram? {
        if (channelId.isNullOrBlank() || details == null) return null
        if (startMs == null || endMs == null) return null
        return XmltvProgram(channelId = channelId, startMs = startMs, endMs = endMs, details = details)
    }
}
