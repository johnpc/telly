package com.johncorser.telly.features.recording

/**
 * What [HlsPlaylistParser] reads out of an `.m3u8` document: either a
 * MASTER playlist (variant streams to choose between) or a MEDIA playlist
 * (the segment list a capture appends in order).
 */
sealed interface HlsPlaylist {
    /** A master playlist: pick the highest-bandwidth [variants] entry. */
    data class Master(
        val variants: List<HlsVariant>,
    ) : HlsPlaylist {
        fun best(): HlsVariant? = variants.maxByOrNull { it.bandwidth }
    }

    /**
     * A media playlist. [segments] carry absolute URLs and monotonically
     * increasing sequence numbers (seeded by `#EXT-X-MEDIA-SEQUENCE`), so a
     * poll loop can dedupe what it already appended. A non-null
     * [initSegmentUrl] (`#EXT-X-MAP`) marks fMP4 segments: those concatenate
     * into a valid fragmented MP4 (init first), never into a TS stream.
     */
    data class Media(
        val segments: List<HlsSegment>,
        val targetDurationMs: Long,
        val ended: Boolean,
        val initSegmentUrl: String?,
    ) : HlsPlaylist
}

/** One `#EXT-X-STREAM-INF` entry of a master playlist. */
data class HlsVariant(
    val url: String,
    val bandwidth: Long,
)

/** One media segment: playlist-order [sequence] plus its absolute [url]. */
data class HlsSegment(
    val sequence: Long,
    val url: String,
)
