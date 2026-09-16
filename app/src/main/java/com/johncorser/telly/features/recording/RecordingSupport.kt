package com.johncorser.telly.features.recording

/**
 * Source classification for the record engine: raw TS / progressive HTTP
 * streams are copied byte-for-byte by [OkHttpStreamRecorder], and `.m3u8`
 * playlists go to [HlsStreamRecorder], which polls the media playlist and
 * concatenates segments (TS segments into a TS stream, fMP4 into a valid
 * fragmented MP4 — see [HlsContainerProbe] for the capture extension).
 */
object RecordingSupport {
    /** Instant record without EPG runs for 3 h (recording-slice spec). */
    const val FALLBACK_DURATION_MS = 3 * 60 * 60_000L

    /** True when [streamUrl] is an HLS playlist the segment recorder owns. */
    fun isHls(streamUrl: String): Boolean {
        val path = streamUrl.substringBefore('#').substringBefore('?').lowercase()
        return path.endsWith(".m3u8")
    }
}
