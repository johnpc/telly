package com.johncorser.telly.features.multiview

import com.johncorser.telly.core.kv.KeyValueStore
import com.johncorser.telly.features.epg.EpgRepository
import com.johncorser.telly.features.playback.PlaybackTime
import com.johncorser.telly.features.player.PlayerEngineFactory
import com.johncorser.telly.features.playlist.db.ChannelDao

/**
 * Everything the multiview screen needs from the composition root. The
 * engine factory yields one independent player per pane (no per-engine
 * audio focus — see Media3PlayerEngine.create); the pool built over it is
 * owned by the screen so every pane's codec is released on exit.
 */
class MultiviewDeps(
    val channelDao: ChannelDao,
    val epgRepository: EpgRepository,
    val engines: PlayerEngineFactory,
    val store: KeyValueStore,
    /** Injected clock + zone + the persisted 12/24-hour choice (picker times). */
    val time: PlaybackTime,
    /** Stream-URL resolution (UDP-proxy rewrite) applied at pane tunes. */
    val resolveUrl: (String) -> String = { it },
)
