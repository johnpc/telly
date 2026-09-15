package com.johncorser.telly.features.playback

import com.johncorser.telly.core.kv.KeyValueStore
import com.johncorser.telly.features.epg.EpgRepository
import com.johncorser.telly.features.player.PlayerEngine
import com.johncorser.telly.features.playlist.db.ChannelDao

/** Everything [PlaybackViewModel] needs injected, bundled for readability. */
class PlaybackEnv(
    val channelDao: ChannelDao,
    val epgRepository: EpgRepository,
    val engine: PlayerEngine,
    val store: KeyValueStore,
    val time: PlaybackTime,
    val hooks: PlaybackHooks = PlaybackHooks(),
)
