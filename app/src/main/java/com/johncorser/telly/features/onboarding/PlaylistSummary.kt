package com.johncorser.telly.features.onboarding

import com.johncorser.telly.features.playlist.M3uChannel
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Pure helpers behind the "Playlist is processed" wizard step. TiviMate
 * classifies `.mp4`/`.mkv` stream URLs as VOD ("Movies") and everything else
 * as live channels, and suggests the source URL's host as the playlist name.
 */
object PlaylistSummary {
    private val movieExtensions = setOf("mp4", "mkv")

    fun movieCount(channels: List<M3uChannel>): Int = channels.count { isMovie(it.streamUrl) }

    fun liveCount(channels: List<M3uChannel>): Int = channels.size - movieCount(channels)

    /** Distinct non-blank `group-title` values, as shown on the stub screen. */
    fun groupCount(channels: List<M3uChannel>): Int =
        channels
            .mapNotNull { channel -> channel.groupTitle?.takeIf(String::isNotBlank) }
            .distinct()
            .size

    /** TiviMate pre-fills the playlist name with the URL's host ("10.0.2.2"). */
    fun suggestName(sourceUrl: String): String = sourceUrl.toHttpUrlOrNull()?.host ?: sourceUrl

    private fun isMovie(streamUrl: String): Boolean =
        streamUrl
            .substringBefore('?')
            .substringAfterLast('.', missingDelimiterValue = "")
            .lowercase() in movieExtensions
}
