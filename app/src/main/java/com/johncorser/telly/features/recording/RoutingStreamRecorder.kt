package com.johncorser.telly.features.recording

import java.io.File

/**
 * Dispatches a capture to the recorder its source type needs: `.m3u8`
 * playlists go to the segment-appending [HlsStreamRecorder], everything
 * else to the byte-copying [OkHttpStreamRecorder]. The engine keeps its
 * single [StreamRecorder] seam.
 */
class RoutingStreamRecorder(
    private val hls: StreamRecorder,
    private val progressive: StreamRecorder,
) : StreamRecorder {
    override suspend fun copy(
        url: String,
        sink: File,
        shouldStop: () -> Boolean,
    ): Long = (if (RecordingSupport.isHls(url)) hls else progressive).copy(url, sink, shouldStop)
}
