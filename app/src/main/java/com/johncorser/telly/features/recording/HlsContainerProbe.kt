package com.johncorser.telly.features.recording

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Names capture files honestly: raw TS / progressive sources record as
 * `.ts`, and HLS playlists are probed once at schedule time — fMP4
 * renditions (`#EXT-X-MAP`) become `.mp4` captures (init + segments
 * concatenate into valid fragmented MP4), TS-segment renditions stay `.ts`.
 * A probe failure falls back to `.ts`; the recorder still captures
 * correctly either way (telly's player sniffs the container).
 */
class HlsContainerProbe(
    private val http: HlsClient = HlsClient(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : StreamContainer {
    override suspend fun extensionOf(streamUrl: String): String {
        if (!RecordingSupport.isHls(streamUrl)) return TS
        return withContext(dispatcher) {
            try {
                if (http.mediaPlaylist(streamUrl).playlist.initSegmentUrl != null) MP4 else TS
            } catch (_: IOException) {
                TS
            }
        }
    }

    companion object {
        private const val TS = "ts"
        private const val MP4 = "mp4"
    }
}
