package com.johncorser.telly.features.guide

import com.johncorser.telly.features.player.external.ExternalPlayerSetting
import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.playlist.db.ChannelOptionsDao
import com.johncorser.telly.features.playlist.db.ChannelOverrides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Persists Channel-options overrides. Every mutation re-reads the FRESH row
 * by id before writing, so consecutive pane edits (rename, then a decoder
 * pick) never clobber one another with a stale entity snapshot; the
 * overrides survive playlist refreshes via ChannelImporter's identity
 * carry-over, exactly like the favorite/hidden/blocked flags.
 */
class ChannelOptionsStore(
    private val dao: ChannelOptionsDao,
    private val scope: CoroutineScope,
) {
    /** The pane re-renders live off this row (rename updates in place). */
    fun observe(channelId: Long): Flow<ChannelEntity?> = dao.observeById(channelId)

    /** Null/blank restores the playlist name. */
    fun rename(
        channelId: Long,
        name: String?,
    ) = mutate(channelId) { it.copy(customName = name?.takeIf(String::isNotBlank)) }

    /** "Hardware"/"Software"; null = follow the global Playback setting. */
    fun setAudioDecoder(
        channelId: Long,
        raw: String?,
    ) = mutate(channelId) { it.copy(audioDecoder = raw) }

    fun setVideoDecoder(
        channelId: Long,
        raw: String?,
    ) = mutate(channelId) { it.copy(videoDecoder = raw) }

    fun setEpgOffset(
        channelId: Long,
        minutes: Int,
    ) = mutate(channelId) { it.copy(epgOffsetMinutes = minutes) }

    /** Flips the EFFECTIVE state into an explicit per-channel override. */
    fun toggleExternal(
        channelId: Long,
        globalOn: Boolean,
    ) = mutateEntity(channelId) { fresh ->
        val effective = ExternalPlayerSetting.overrideOf(fresh.overrides.externalPlayer) ?: globalOn
        val next = if (effective) ExternalPlayerSetting.OFF else ExternalPlayerSetting.ON
        fresh.copy(overrides = fresh.overrides.copy(externalPlayer = next))
    }

    private fun mutate(
        channelId: Long,
        transform: (ChannelOverrides) -> ChannelOverrides,
    ) = mutateEntity(channelId) { it.copy(overrides = transform(it.overrides)) }

    private fun mutateEntity(
        channelId: Long,
        transform: (ChannelEntity) -> ChannelEntity,
    ) {
        scope.launch { dao.byId(channelId)?.let { dao.update(transform(it)) } }
    }
}
