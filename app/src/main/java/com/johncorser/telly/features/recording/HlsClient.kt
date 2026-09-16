package com.johncorser.telly.features.recording

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/** A media playlist together with the URL it was fetched from (re-pollable). */
data class ResolvedHlsMedia(
    val url: String,
    val playlist: HlsPlaylist.Media,
)

/**
 * The HTTP half of the HLS capture path: fetches playlists (following the
 * master → highest-bandwidth-variant hop) and appends segment bodies onto
 * the capture file, sending the same stream User-Agent and following the
 * same redirects as [OkHttpStreamRecorder]. Failures throw [IOException]
 * so callers keep the engine's reconnect/idle-failure policy.
 */
class HlsClient(
    private val client: OkHttpClient = defaultClient(),
) {
    /** Fetches [url] and hops master playlists onto their best variant. */
    fun mediaPlaylist(url: String): ResolvedHlsMedia =
        when (val playlist = HlsPlaylistParser.parse(fetchText(url), url)) {
            is HlsPlaylist.Media -> ResolvedHlsMedia(url, playlist)
            is HlsPlaylist.Master -> variantMedia(playlist)
        }

    /** Appends the body of [url] onto [sink]; returns the bytes written. */
    fun appendBody(
        url: String,
        sink: File,
    ): Long =
        fetch(url).use { response ->
            val body = response.body ?: return@use 0L
            body.byteStream().use { stream ->
                FileOutputStream(sink, true).use { out -> stream.copyTo(out) }
            }
        }

    private fun variantMedia(master: HlsPlaylist.Master): ResolvedHlsMedia {
        val variant = master.best() ?: throw IOException("HLS master playlist lists no variants")
        val playlist =
            HlsPlaylistParser.parse(fetchText(variant.url), variant.url) as? HlsPlaylist.Media
                ?: throw IOException("HLS variant did not resolve to a media playlist")
        return ResolvedHlsMedia(variant.url, playlist)
    }

    private fun fetchText(url: String): String = fetch(url).use { it.body?.string().orEmpty() }

    private fun fetch(url: String): Response {
        val request = Request.Builder().url(url).header("User-Agent", OkHttpStreamRecorder.USER_AGENT).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            response.close()
            throw IOException("HTTP ${response.code} while recording HLS")
        }
        return response
    }

    companion object {
        private fun defaultClient(): OkHttpClient =
            OkHttpClient
                .Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .build()
    }
}
