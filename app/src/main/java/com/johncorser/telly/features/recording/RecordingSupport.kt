package com.johncorser.telly.features.recording

/**
 * What telly's record engine can capture: raw TS / progressive HTTP streams
 * copied byte-for-byte. HLS playlists (`.m3u8`) would need the variant's
 * segment list polled and concatenated — an honest later slice, so Record
 * on an HLS channel explains itself instead of producing a broken file.
 */
object RecordingSupport {
    /** Instant record without EPG runs for 3 h (recording-slice spec). */
    const val FALLBACK_DURATION_MS = 3 * 60 * 60_000L

    /** Verbatim copy for the "can't record HLS yet" explainer screens. */
    const val HLS_MESSAGE =
        "This channel streams over HLS, which telly cannot record yet. " +
            "Raw TS and progressive HTTP channels can be recorded."

    /** True when the plain HTTP byte copy can capture [streamUrl]. */
    fun isRecordable(streamUrl: String): Boolean {
        val path = streamUrl.substringBefore('#').substringBefore('?').lowercase()
        return !path.endsWith(".m3u8")
    }
}
