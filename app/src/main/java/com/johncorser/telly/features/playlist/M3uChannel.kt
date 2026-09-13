package com.johncorser.telly.features.playlist

/** One live channel: an `#EXTINF` entry paired with its stream URL. */
data class M3uChannel(
    val title: String,
    val streamUrl: String,
    val tvgId: String? = null,
    val tvgName: String? = null,
    val tvgLogo: String? = null,
    val groupTitle: String? = null,
)

/** A fully parsed M3U playlist: optional `url-tvg` EPG hint plus channels. */
data class M3uPlaylist(
    val epgUrl: String? = null,
    val channels: List<M3uChannel> = emptyList(),
)

/** Lifts a parsed `#EXTINF` entry into a channel once its URL line arrives. */
internal fun M3uEntry.toChannel(streamUrl: String): M3uChannel =
    M3uChannel(
        title = title,
        streamUrl = streamUrl,
        tvgId = attributes["tvg-id"],
        tvgName = attributes["tvg-name"],
        tvgLogo = attributes["tvg-logo"],
        groupTitle = attributes["group-title"],
    )
