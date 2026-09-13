package com.johncorser.telly.features.playback

import com.johncorser.telly.features.epg.EpgRepository
import com.johncorser.telly.features.epg.NowNext
import com.johncorser.telly.features.player.VideoDetails
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.TimeZone

/**
 * Streams the ready-to-render info-overlay model for the tuned channel.
 * Now/next comes from Room via [EpgRepository] keyed on the instant the
 * overlay was opened, so data stays live-updating yet clock-injection keeps
 * tests deterministic.
 */
class PlaybackInfoFeed(
    current: Flow<ChannelEntity?>,
    instant: Flow<Long>,
    video: Flow<VideoDetails?>,
    private val epgRepository: EpgRepository,
    private val zone: TimeZone,
    scope: CoroutineScope,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val nowNext: Flow<Pair<NowNext, Long>> =
        combine(current, instant) { channel, at -> channel to at }
            .flatMapLatest { (channel, at) -> nowNextOf(channel, at).map { it to at } }

    val info: StateFlow<PlaybackInfoData?> =
        combine(current, nowNext, video) { channel, (guide, at), videoDetails ->
            channel?.let { PlaybackInfoBuilder.build(it, guide, videoDetails, at, zone) }
        }.stateIn(scope, SharingStarted.Eagerly, null)

    private fun nowNextOf(
        channel: ChannelEntity?,
        atMs: Long,
    ): Flow<NowNext> {
        val tvgId = channel?.source?.tvgId ?: return flowOf(NowNext())
        return epgRepository.nowNext(listOf(tvgId), atMs).map { it[tvgId] ?: NowNext() }
    }
}
