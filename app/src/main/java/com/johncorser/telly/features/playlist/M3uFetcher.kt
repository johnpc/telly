package com.johncorser.telly.features.playlist

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

/**
 * Downloads an M3U playlist body over HTTP(S). [userAgentFor] supplies the
 * per-playlist User-Agent (per-playlist setting > global setting); null
 * keeps the client default.
 */
class M3uFetcher(
    private val client: OkHttpClient = OkHttpClient(),
    private val userAgentFor: (url: String) -> String? = { null },
) {
    /** Returns the playlist body, or throws [IOException] on any HTTP failure. */
    suspend fun fetch(url: String): String =
        withContext(Dispatchers.IO) {
            val request =
                Request
                    .Builder()
                    .url(url)
                    .apply { userAgentFor(url)?.let { header("User-Agent", it) } }
                    .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("HTTP ${response.code} while downloading playlist")
                }
                response.body?.string().orEmpty()
            }
        }
}
