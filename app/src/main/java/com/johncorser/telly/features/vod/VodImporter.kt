package com.johncorser.telly.features.vod

import com.johncorser.telly.features.playlist.M3uChannel
import com.johncorser.telly.features.vod.db.VodItemEntity

/**
 * Turns the VOD-classified playlist entries into vod_items rows, preserving
 * group-title as the browser category and tvg-logo as the card artwork.
 */
object VodImporter {
    fun import(
        playlistId: Long,
        parsed: List<M3uChannel>,
    ): List<VodItemEntity> =
        parsed.mapIndexed { index, entry ->
            VodItemEntity(
                playlistId = playlistId,
                sortIndex = index,
                itemKey = VodClassifier.itemKey(entry.streamUrl, entry.title),
                name = entry.title,
                groupTitle = entry.groupTitle,
                logoUrl = entry.tvgLogo,
                streamUrl = entry.streamUrl,
            )
        }
}
