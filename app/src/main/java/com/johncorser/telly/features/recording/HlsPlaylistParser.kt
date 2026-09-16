package com.johncorser.telly.features.recording

import java.net.URI

/**
 * Pure `.m3u8` reader for the HLS capture path: master-vs-media detection,
 * variant lists, segment lists with sequence numbers, target duration,
 * `#EXT-X-ENDLIST` and `#EXT-X-MAP` (fMP4) detection. Relative URIs are
 * resolved against the playlist's own URL. No I/O, fully JVM-tested.
 */
object HlsPlaylistParser {
    fun parse(
        text: String,
        playlistUrl: String,
    ): HlsPlaylist {
        val lines = text.lines().map(String::trim).filter(String::isNotEmpty)
        return if (lines.any { it.startsWith(STREAM_INF) }) {
            master(lines, playlistUrl)
        } else {
            media(lines, playlistUrl)
        }
    }

    /** Resolves a playlist [reference] (possibly relative) against [baseUrl]. */
    fun resolve(
        baseUrl: String,
        reference: String,
    ): String = runCatching { URI(baseUrl).resolve(reference).toString() }.getOrDefault(reference)

    private fun master(
        lines: List<String>,
        baseUrl: String,
    ): HlsPlaylist.Master {
        val variants = mutableListOf<HlsVariant>()
        var pendingBandwidth: Long? = null
        for (line in lines) {
            when {
                line.startsWith(STREAM_INF) -> pendingBandwidth = bandwidthOf(line)
                isUri(line) && pendingBandwidth != null -> {
                    variants += HlsVariant(url = resolve(baseUrl, line), bandwidth = pendingBandwidth)
                    pendingBandwidth = null
                }
            }
        }
        return HlsPlaylist.Master(variants)
    }

    private fun media(
        lines: List<String>,
        baseUrl: String,
    ): HlsPlaylist.Media =
        HlsPlaylist.Media(
            segments = segmentsOf(lines, baseUrl),
            targetDurationMs = targetDurationMsOf(lines),
            ended = lines.any { it == END_LIST },
            initSegmentUrl = initSegmentUrlOf(lines, baseUrl),
        )

    private fun segmentsOf(
        lines: List<String>,
        baseUrl: String,
    ): List<HlsSegment> {
        var sequence = taggedValue(lines, MEDIA_SEQUENCE)?.toLongOrNull() ?: 0L
        return lines.filter(::isUri).map { line -> HlsSegment(sequence = sequence++, url = resolve(baseUrl, line)) }
    }

    private fun targetDurationMsOf(lines: List<String>): Long {
        val seconds = taggedValue(lines, TARGET_DURATION)?.toLongOrNull() ?: return DEFAULT_TARGET_DURATION_MS
        return seconds * MS_PER_SECOND
    }

    private fun initSegmentUrlOf(
        lines: List<String>,
        baseUrl: String,
    ): String? =
        taggedValue(lines, MAP)?.let { attributes ->
            URI_PATTERN.find(attributes)?.groupValues?.get(1)?.let { resolve(baseUrl, it) }
        }

    private fun taggedValue(
        lines: List<String>,
        tag: String,
    ): String? = lines.firstOrNull { it.startsWith(tag) }?.removePrefix(tag)

    private fun isUri(line: String): Boolean = !line.startsWith("#")

    private fun bandwidthOf(streamInfLine: String): Long =
        BANDWIDTH_PATTERN.find(streamInfLine)?.groupValues?.get(1)?.toLongOrNull() ?: 0L

    private const val STREAM_INF = "#EXT-X-STREAM-INF"
    private const val MEDIA_SEQUENCE = "#EXT-X-MEDIA-SEQUENCE:"
    private const val TARGET_DURATION = "#EXT-X-TARGETDURATION:"
    private const val MAP = "#EXT-X-MAP:"
    private const val END_LIST = "#EXT-X-ENDLIST"
    private const val MS_PER_SECOND = 1_000L

    /** The spec requires TARGETDURATION; a sane poll cadence if it's absent. */
    const val DEFAULT_TARGET_DURATION_MS = 6_000L

    private val BANDWIDTH_PATTERN = Regex("""BANDWIDTH=(\d+)""")
    private val URI_PATTERN = Regex("""URI="([^"]+)"""")
}
