package com.johncorser.telly.features.recording

import com.johncorser.telly.features.player.STREAM_USER_AGENT
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
 * the capture file, resolving the stream User-Agent per URL through
 * [userAgentFor] (the same per-playlist > global > default precedence as
 * [OkHttpStreamRecorder]) and following the same redirects. Failures throw
 * [IOException] so callers keep the engine's reconnect/idle-failure policy.
 * Every request registers with the caller's [RecordingCalls] so a user
 * stop can cancel a blocked fetch instead of waiting it out.
 */
class HlsClient(
    private val client: OkHttpClient = recordingHttpClient(),
    private val userAgentFor: (streamUrl: String) -> String = { STREAM_USER_AGENT },
) {
    /** Fetches [url] and hops master playlists onto their best variant. */
    fun mediaPlaylist(
        url: String,
        calls: RecordingCalls = RecordingCalls(),
    ): ResolvedHlsMedia =
        when (val playlist = HlsPlaylistParser.parse(fetchText(url, calls), url)) {
            is HlsPlaylist.Media -> ResolvedHlsMedia(url, playlist)
            is HlsPlaylist.Master -> variantMedia(playlist, calls)
        }

    /** Appends the body of [url] onto [sink]; returns the bytes written. */
    fun appendBody(
        url: String,
        sink: File,
        calls: RecordingCalls = RecordingCalls(),
    ): Long =
        fetch(url, calls).use { response ->
            val body = response.body ?: return@use 0L
            body.byteStream().use { stream ->
                FileOutputStream(sink, true).use { out -> stream.copyTo(out) }
            }
        }

    private fun variantMedia(
        master: HlsPlaylist.Master,
        calls: RecordingCalls,
    ): ResolvedHlsMedia {
        val variant = master.best() ?: throw IOException("HLS master playlist lists no variants")
        val playlist =
            HlsPlaylistParser.parse(fetchText(variant.url, calls), variant.url) as? HlsPlaylist.Media
                ?: throw IOException("HLS variant did not resolve to a media playlist")
        return ResolvedHlsMedia(variant.url, playlist)
    }

    private fun fetchText(
        url: String,
        calls: RecordingCalls,
    ): String = fetch(url, calls).use { it.body?.string().orEmpty() }

    private fun fetch(
        url: String,
        calls: RecordingCalls,
    ): Response {
        val request = Request.Builder().url(url).header("User-Agent", userAgentFor(url)).build()
        val response = calls.track(client.newCall(request)).execute()
        if (!response.isSuccessful) {
            response.close()
            throw IOException("HTTP ${response.code} while recording HLS")
        }
        return response
    }
}
