package com.johncorser.telly.features.playlist

import com.johncorser.telly.features.playlist.db.ChannelCatchup
import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.playlist.db.ChannelFlags
import com.johncorser.telly.features.playlist.db.ChannelOverrides
import com.johncorser.telly.features.playlist.db.ChannelSource

/**
 * Turns parsed M3U channels into channel rows. Channel numbers are assigned
 * sequentially from playlist order (TiviMate default, ux-spec §5). User
 * flags (favorite/hidden/blocked) and the Channel-options overrides survive
 * refreshes via [identityOf]: a channel is "the same" when its tvg-id
 * matches, falling back to stream URL + name.
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
        val carried =
            previous.associateBy { channel ->
                identityOf(channel.source.tvgId, channel.source.streamUrl, channel.source.name)
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
                    carried[identityOf(channel.tvgId, channel.streamUrl, channel.title)]?.flags
                        ?: ChannelFlags(),
                overrides =
                    carried[identityOf(channel.tvgId, channel.streamUrl, channel.title)]?.overrides
                        ?: ChannelOverrides(),
                catchup =
                    ChannelCatchup(
                        catchupType = channel.catchup,
                        catchupSource = channel.catchupSource,
                        catchupDays = channel.catchupDays,
                    ),
            )
        }
    }
}
