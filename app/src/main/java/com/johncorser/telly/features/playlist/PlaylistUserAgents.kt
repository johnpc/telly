package com.johncorser.telly.features.playlist

/**
 * User-Agent precedence shared by playlist fetches and stream playback:
 * the playlist's own User-Agent setting when set, else the global General
 * setting, else null (the caller's default applies).
 */
class UserAgentPrecedence(
    private val perPlaylist: (playlistUrl: String) -> String,
    private val global: () -> String,
) {
    fun forPlaylist(playlistUrl: String?): String? =
        playlistUrl?.let { nonBlank(perPlaylist(it)) } ?: nonBlank(global())

    private fun nonBlank(value: String): String? = value.trim().ifEmpty { null }
}

/**
 * Maps a channel's stream URL to the User-Agent its HTTP requests must
 * send: the owning playlist's UA > the global setting > [fallback]. The
 * [playlistUrlFor] lookup may block (a Room query): the player calls
 * [resolve] from a loader thread, never the main thread.
 */
class StreamUserAgentResolver(
    private val playlistUrlFor: (streamUrl: String) -> String?,
    private val precedence: UserAgentPrecedence,
    private val fallback: String,
) {
    fun resolve(streamUrl: String): String = precedence.forPlaylist(playlistUrlFor(streamUrl)) ?: fallback
}
