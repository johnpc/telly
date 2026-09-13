package com.johncorser.telly.features.playlist

/** A single channel entry parsed from an `#EXTINF` playlist line. */
data class M3uEntry(
    val title: String,
    val attributes: Map<String, String>,
)

/**
 * Minimal parser for M3U `#EXTINF` lines — the walking-skeleton seed of the
 * playlist slice. Full playlist parsing (stream URLs, groups, EPG hints)
 * arrives with the first real slice.
 */
object M3uParser {
    const val HEADER = "#EXTM3U"
    private const val EXTINF = "#EXTINF"
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
}
