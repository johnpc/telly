package com.johncorser.telly.features.playlist

/** A single channel entry parsed from an `#EXTINF` playlist line. */
data class M3uEntry(
    val title: String,
    val attributes: Map<String, String>,
)

/** Parser for M3U playlists: `#EXTINF` entries, stream URLs, `url-tvg` hint. */
object M3uParser {
    const val HEADER = "#EXTM3U"
    private const val EXTINF = "#EXTINF"
    private const val COMMENT = "#"
    private const val URL_TVG = "url-tvg"
    private val attributePattern = Regex("([\\w-]+)=\"([^\"]*)\"")

    fun isHeader(line: String): Boolean = line.trim().startsWith(HEADER)

    fun isExtInf(line: String): Boolean = line.trim().startsWith(EXTINF)

    /** Parses one `#EXTINF:-1 key="value",Title` line; null when it is not one. */
    fun parseExtInf(line: String): M3uEntry? {
        if (!isExtInf(line)) return null
        val body = line.trim().substringAfter(':', missingDelimiterValue = "")
        val title = body.substringAfterLast(',', missingDelimiterValue = "").trim()
        if (title.isEmpty()) return null
        val attributes =
            attributePattern
                .findAll(body)
                .associate { it.groupValues[1] to it.groupValues[2] }
        return M3uEntry(title = title, attributes = attributes)
    }

    /** Attributes on an `#EXTM3U` header line (e.g. `url-tvg`); empty otherwise. */
    fun headerAttributes(line: String): Map<String, String> =
        if (isHeader(line)) {
            attributePattern.findAll(line).associate { it.groupValues[1] to it.groupValues[2] }
        } else {
            emptyMap()
        }

    /** Parses a whole playlist body into channels plus the EPG URL hint. */
    fun parse(content: String): M3uPlaylist {
        val accumulator = ParseAccumulator()
        content.lineSequence().forEach { accumulator.accept(it.trim()) }
        return accumulator.build()
    }

    /** Line-by-line parse state: the EPG hint plus the EXTINF awaiting its URL. */
    private class ParseAccumulator {
        private var epgUrl: String? = null
        private var pending: M3uEntry? = null
        private val channels = mutableListOf<M3uChannel>()

        fun accept(line: String) {
            when {
                line.isEmpty() -> Unit
                isHeader(line) -> epgUrl = headerAttributes(line)[URL_TVG] ?: epgUrl
                isExtInf(line) -> pending = parseExtInf(line)
                line.startsWith(COMMENT) -> Unit
                else -> emit(line)
            }
        }

        private fun emit(streamUrl: String) {
            pending?.let { channels += it.toChannel(streamUrl) }
            pending = null
        }

        fun build(): M3uPlaylist = M3uPlaylist(epgUrl = epgUrl, channels = channels)
    }
}
