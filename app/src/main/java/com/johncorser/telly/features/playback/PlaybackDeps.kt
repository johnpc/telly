package com.johncorser.telly.features.playback

import com.johncorser.telly.core.kv.KeyValueStore
import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.features.epg.EpgRepository
import com.johncorser.telly.features.history.WatchHistory
import com.johncorser.telly.features.player.Media3PlayerEngine
import com.johncorser.telly.features.playlist.db.ChannelDao

/**
 * Everything the playback screen needs from the composition root. The engine
 * comes as a factory so each visit to the playback route gets a fresh
 * ExoPlayer that is released when the screen leaves composition.
 */
class PlaybackDeps(
    val channelDao: ChannelDao,
    val epgRepository: EpgRepository,
    val keyValueStore: KeyValueStore,
    val history: WatchHistory,
    val engineFactory: () -> Media3PlayerEngine,
    val clock: () -> Long,
    val parental: ParentalControls? = null,
)
