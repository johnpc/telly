package com.johncorser.telly.features.player

/**
 * The User-Agent one engine's HTTP requests send. HLS segment URLs differ
 * from the channel's stream URL, so the engine records the tuned stream URL
 * at load time and every request (manifest and segments alike) resolves the
 * UA for THAT url. Resolution is lazy — [current] runs on the player's
 * loader thread, where the resolver's Room lookup may block — and cached
 * per stream URL so one tune costs one lookup.
 */
class StreamUserAgent(
    private val resolve: (streamUrl: String) -> String,
    private val default: String = STREAM_USER_AGENT,
) {
    @Volatile
    private var streamUrl: String? = null

    @Volatile
    private var cached: Pair<String, String>? = null

    /** The engine tuned [url]; requests resolve against it from now on. */
    fun onLoad(url: String) {
        streamUrl = url
    }

    /** The User-Agent for the current stream (loader thread, cached). */
    fun current(): String {
        val url = streamUrl ?: return default
        cached?.takeIf { it.first == url }?.let { return it.second }
        return resolve(url).also { cached = url to it }
    }
}
