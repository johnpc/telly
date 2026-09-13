package com.johncorser.telly.features.playlist

import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.playlist.db.ChannelFlags
import com.johncorser.telly.features.playlist.db.ChannelSource

/**
 * Turns parsed M3U channels into channel rows. Channel numbers are assigned
 * sequentially from playlist order (TiviMate default, ux-spec §5). Favorite
 * and hidden flags survive refreshes via [identityOf]: a channel is "the
 * same" when its tvg-id matches, falling back to stream URL + name.
 */
object ChannelImporter {
    /** Stable identity used to carry user flags across playlist refreshes. */
    fun identityOf(
        tvgId: String?,
        streamUrl: String,
        name: String,
    ): String = tvgId?.takeIf { it.isNotBlank() } ?: "$streamUrl|$name"

    /** Builds the replacement rows for [playlistId] from a fresh parse. */
    fun import(
        playlistId: Long,
        parsed: List<M3uChannel>,
        previous: List<ChannelEntity>,
    ): List<ChannelEntity> {
        val previousFlags =
            previous.associate { channel ->
                identityOf(channel.source.tvgId, channel.source.streamUrl, channel.source.name) to
                    channel.flags
            }
        return parsed.mapIndexed { index, channel ->
            ChannelEntity(
                playlistId = playlistId,
                number = index + 1,
                sortIndex = index,
                source =
                    ChannelSource(
                        name = channel.title,
                        groupTitle = channel.groupTitle,
                        logoUrl = channel.tvgLogo,
                        streamUrl = channel.streamUrl,
                        tvgId = channel.tvgId,
                    ),
                flags =
                    previousFlags[identityOf(channel.tvgId, channel.streamUrl, channel.title)]
                        ?: ChannelFlags(),
            )
        }
    }
}
