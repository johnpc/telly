package com.johncorser.telly.features.recording

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * The production [StreamRecorder]: a plain OkHttp GET appended chunk by
 * chunk onto the capture file. Redirects (including the cross-protocol
 * token/load-balancer hops IPTV providers love) are followed and the app's
 * stream User-Agent is sent — the same treatment the player's
 * StreamMediaSourceFactory gives the same URLs.
 */
class OkHttpStreamRecorder(
    private val client: OkHttpClient = defaultClient(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : StreamRecorder {
    override suspend fun copy(
        url: String,
        sink: File,
        shouldStop: () -> Boolean,
    ): Long =
        withContext(dispatcher) {
            val request = Request.Builder().url(url).header("User-Agent", USER_AGENT).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ${response.code} while recording")
                val body = response.body ?: return@use 0L
                body.byteStream().use { stream -> drain(stream, sink, shouldStop) }
            }
        }

    private fun drain(
        stream: InputStream,
        sink: File,
        shouldStop: () -> Boolean,
    ): Long {
        var written = 0L
        FileOutputStream(sink, true).use { out ->
            val buffer = ByteArray(CHUNK_BYTES)
            while (!shouldStop()) {
                val read = stream.read(buffer)
                if (read < 0) break
                out.write(buffer, 0, read)
                written += read
            }
        }
        return written
    }

    companion object {
        private const val CHUNK_BYTES = 64 * 1024

        /** Same client UA the player sends; some providers gate streams on it. */
        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 11; SHIELD Android TV) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

        private fun defaultClient(): OkHttpClient =
            OkHttpClient
                .Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .build()
    }
}
