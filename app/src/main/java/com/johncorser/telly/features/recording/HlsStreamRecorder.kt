package com.johncorser.telly.features.recording

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

/**
 * The HLS [StreamRecorder]: polls the media playlist per target duration
 * and appends every new segment, in order, onto the capture file — TS
 * segments concatenate into a valid TS stream, fMP4 (`#EXT-X-MAP`) captures
 * get their init segment first and concatenate into a valid fragmented MP4
 * (the capture file was named `.mp4` by [HlsContainerProbe]). Progress is
 * deduped by sequence number per capture file, so the engine's reconnect
 * attempts never append a segment twice; VOD playlists (`#EXT-X-ENDLIST`)
 * finish the attempt, live ones run until the engine's stop/planned end.
 */
class HlsStreamRecorder(
    private val http: HlsClient = HlsClient(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val pollDelay: suspend (Long, () -> Boolean) -> Unit = ::awaitNextHlsPoll,
    private val log: (String) -> Unit = {},
) : StreamRecorder {
    private class CaptureProgress {
        @Volatile var lastSequence = NO_SEQUENCE

        @Volatile var initWritten = false
    }

    private val progressBySink = ConcurrentHashMap<String, CaptureProgress>()

    override suspend fun copy(
        url: String,
        sink: File,
        shouldStop: () -> Boolean,
    ): Long =
        withContext(dispatcher) {
            val progress = progressBySink.getOrPut(sink.path) { CaptureProgress() }
            var written = 0L
            try {
                var media = http.mediaPlaylist(url)
                var live = !shouldStop()
                while (live) {
                    written += append(media.playlist, sink, progress, shouldStop)
                    live = !media.playlist.ended && !shouldStop()
                    if (live) {
                        pollDelay(media.playlist.targetDurationMs, shouldStop)
                        live = !shouldStop()
                        if (live) media = http.mediaPlaylist(media.url)
                    }
                }
            } catch (e: IOException) {
                // No progress this attempt -> let the engine count it idle;
                // otherwise report the bytes and reconnect on the next attempt.
                if (written == 0L) throw e
                log("HLS capture interrupted (${e.message}); reconnecting")
            }
            written
        }

    /** Appends the init segment (once) plus every not-yet-written segment. */
    private fun append(
        playlist: HlsPlaylist.Media,
        sink: File,
        progress: CaptureProgress,
        shouldStop: () -> Boolean,
    ): Long {
        var written = initBytes(playlist, sink, progress)
        val fresh = playlist.segments.filter { it.sequence > progress.lastSequence }
        logMissed(fresh, progress)
        for (segment in fresh) {
            if (shouldStop()) break
            written += http.appendBody(segment.url, sink)
            progress.lastSequence = segment.sequence
        }
        return written
    }

    private fun initBytes(
        playlist: HlsPlaylist.Media,
        sink: File,
        progress: CaptureProgress,
    ): Long {
        val initUrl = playlist.initSegmentUrl
        if (initUrl == null || progress.initWritten) return 0L
        val written = http.appendBody(initUrl, sink)
        progress.initWritten = true
        return written
    }

    /** The playlist window slid past what we captured: log and continue. */
    private fun logMissed(
        fresh: List<HlsSegment>,
        progress: CaptureProgress,
    ) {
        val next = fresh.firstOrNull()?.sequence ?: return
        if (progress.lastSequence != NO_SEQUENCE && next > progress.lastSequence + 1) {
            log("HLS capture missed segments ${progress.lastSequence + 1}..${next - 1}; continuing")
        }
    }

    companion object {
        private const val NO_SEQUENCE = -1L
    }
}

/**
 * The default between-polls wait, sliced so a user stop lands promptly —
 * one plain target-duration delay kept the engine's stop() joined for
 * whole seconds while the row still said RECORDING.
 */
internal suspend fun awaitNextHlsPoll(
    totalMs: Long,
    shouldStop: () -> Boolean,
) {
    var waitedMs = 0L
    while (waitedMs < totalMs && !shouldStop()) {
        delay(HLS_STOP_CHECK_SLICE_MS.coerceAtMost(totalMs - waitedMs))
        waitedMs += HLS_STOP_CHECK_SLICE_MS
    }
}

internal const val HLS_STOP_CHECK_SLICE_MS = 250L
