package com.johncorser.telly.features.recording

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
            val calls = RecordingCalls()
            cancellingOnStop(calls, shouldStop) { attempt(url, sink, shouldStop, calls) }
        }

    private suspend fun attempt(
        url: String,
        sink: File,
        shouldStop: () -> Boolean,
        calls: RecordingCalls,
    ): Long {
        val progress = progressBySink.getOrPut(sink.path) { CaptureProgress() }
        var written = 0L
        try {
            var media: ResolvedHlsMedia? = http.mediaPlaylist(url, calls)
            while (media != null && !shouldStop()) {
                written += append(media.playlist, sink, progress, shouldStop, calls)
                media = nextPoll(media, shouldStop, calls)
            }
        } catch (e: IOException) {
            // A stop-cancelled fetch ends the attempt normally. Otherwise no
            // progress -> let the engine count it idle; progress -> report
            // the bytes and reconnect on the next attempt.
            if (!shouldStop()) {
                if (written == 0L) throw e
                log("HLS capture interrupted (${e.message}); reconnecting")
            }
        }
        return written
    }

    /** Waits out the target duration, then re-polls the live playlist (null ends the attempt). */
    private suspend fun nextPoll(
        media: ResolvedHlsMedia,
        shouldStop: () -> Boolean,
        calls: RecordingCalls,
    ): ResolvedHlsMedia? {
        if (media.playlist.ended || shouldStop()) return null
        pollDelay(media.playlist.targetDurationMs, shouldStop)
        return if (shouldStop()) null else http.mediaPlaylist(media.url, calls)
    }

    /** Appends the init segment (once) plus every not-yet-written segment. */
    private fun append(
        playlist: HlsPlaylist.Media,
        sink: File,
        progress: CaptureProgress,
        shouldStop: () -> Boolean,
        calls: RecordingCalls,
    ): Long {
        var written = initBytes(playlist, sink, progress, calls)
        val fresh = playlist.segments.filter { it.sequence > progress.lastSequence }
        logMissed(fresh, progress)
        for (segment in fresh) {
            if (shouldStop()) break
            written += http.appendBody(segment.url, sink, calls)
            progress.lastSequence = segment.sequence
        }
        return written
    }

    private fun initBytes(
        playlist: HlsPlaylist.Media,
        sink: File,
        progress: CaptureProgress,
        calls: RecordingCalls,
    ): Long {
        val initUrl = playlist.initSegmentUrl
        if (initUrl == null || progress.initWritten) return 0L
        val written = http.appendBody(initUrl, sink, calls)
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
